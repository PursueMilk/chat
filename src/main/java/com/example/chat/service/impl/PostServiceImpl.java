package com.example.chat.service.impl;


import com.example.chat.async.RabbitProduce;
import com.example.chat.dto.CommentDto;
import com.example.chat.exception.ChatException;
import com.example.chat.filter.SensitiveFilter;
import com.example.chat.mapper.PostMapper;
import com.example.chat.mapper.ScoreMapper;
import com.example.chat.pojo.*;
import com.example.chat.service.*;
import com.example.chat.utils.ConstantUtil;
import com.example.chat.utils.ImgUtil;
import com.example.chat.utils.RedisUtil;
import com.example.chat.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.example.chat.utils.ConstantUtil.ENTITY_TYPE_POST;
import static com.example.chat.utils.ConstantUtil.TOPIC_PUBLISH;
import static com.example.chat.utils.RedisKeyUtil.POST_SCORE;
import static com.example.chat.utils.RedisKeyUtil.PREFIX_POST_CHANGE;


@Service
public class PostServiceImpl implements PostService {


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitProduce rabbitProduce;

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private SensitiveFilter filter;

    @Value("${server.port}")
    private String port;

    private static final String fileName = "post";

    //从数据库加载热榜
    @PostConstruct
    public void init() {
        List<PostScore> list = scoreMapper.hotList();
        redisUtil.setHotList(POST_SCORE, list);
    }


    //发布文章
    @Override
    public Result publish(Post post) {
        //设置文章类型，默认普通类型，Type为0
        post.setType(0);
        //处理转义字符
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        //转义HTML标记
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(filter.filter(post.getTitle()));
        post.setContent(filter.filter(post.getContent()));
        postMapper.insertPost(post);
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(post.getUserId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        rabbitProduce.handleTask(event);
        return Result.success().setData(post.getId());
    }

    //首页文章列表
    @Override
    public PaginationVo<PostVo> posts(int currentPage, int listMode) {
        int offset = (currentPage - 1) * 5;
        int total = 0;
        //按顶置、发布时间排序
        List<Post> posts = null;
        total = postMapper.countPost();
        if (listMode == 0) {
            //最新推文
            posts = postMapper.listPost(offset);
        } else {
            //按热度获取
            Set<Integer> set = redisUtil.getHotPostId(offset, 5);
            if (set != null && set.size() > 0) {
                posts = postMapper.queryPostByIds(set);
            }
        }
        //文章点赞、评论等
        List<PostVo> postVos = getPostMessage(posts);
        //分页设置
        PaginationVo paginationVo = new PaginationVo<PostVo>();
        paginationVo.setCurrentPage(currentPage);
        paginationVo.setTotal(total);
        paginationVo.setPageSize(5);
        paginationVo.setRecords(postVos);
        return paginationVo;
    }


    //文章详情
    @Override
    public Result postDetail(int pid, int userId, boolean state) {
        Post post = postCache(pid);
        //查询不出文章
        if (Objects.isNull(post)) {
            throw new ChatException("文章不存在");
        }
        //设置点赞数
        long likeCount = likeService.getPostLikeCount(pid);
        post.setLikeCount(likeCount);
        //设置收藏数
        long collectCount = collectService.getPostCollectCount(pid);
        post.setCollectCount(collectCount);
        //作者
        User user = userService.getUserById(post.getUserId());
        int likeStatus = 0;
        int collectStatus = 0;
        //是否登录
        if (state) {
            // 点赞状态
            likeStatus = likeService.getPostLikeStatus(userId, pid);
            // 收藏状态
            collectStatus = collectService.getPostCollectStatus(userId, pid);
        }
        //设置返回信息，如文章信息、评论信息，用户状态
        PostDetailVo postDetailVo = new PostDetailVo();
        postDetailVo.setLikeStatus(likeStatus);
        postDetailVo.setCollectStatus(collectStatus);
        PostVo postVo = new PostVo();
        postVo.setPost(post);
        postVo.setUser(user);
        //一级评论列表，默认前5条,最新的在最上面
        List<Comment> commentList = commentService.getCommentsByEntity(ENTITY_TYPE_POST, post.getId(), 0, 5);
        //一级评论vo集合
        List<CommentVo> commentVoList = getCommentVoList(commentList, state, userId);
        postDetailVo.setComments(commentVoList);
        postDetailVo.setPostVo(postVo);
        return Result.success().setData(postDetailVo);
    }

    public Post postCache(int postId) {
        return redisUtil.queryWithMutex(PREFIX_POST_CHANGE, postId, Post.class, new Function() {
            @Override
            public Object apply(Object o) {
                Post post = postMapper.queryPostById(postId);
                //文章为null则直接缓存null,解决缓存穿透问题
                if (Objects.isNull(post)) {
                    return null;
                }
                post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
                post.setTitle(HtmlUtils.htmlUnescape(post.getTitle()));
                //转化日期格式
                String strDateFormat = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                post.setCreateTimeStr(sdf.format(post.getCreateTime()));
                return post;
            }
        }, 60L, TimeUnit.MINUTES);
    }


    @Override
    public Result CommentList(CommentDto commentDto, int userId, boolean state) {
        //一级评论列表
        int offset = (commentDto.getCurrentPage() - 1) * 5;
        List<Comment> commentList = commentService.getCommentsByEntity(
                ENTITY_TYPE_POST, commentDto.getPid(), offset, 5);
        //一级评论vo集合
        List<CommentVo> commentVoList = getCommentVoList(commentList, state, userId);
        PaginationVo<CommentVo> paginationVo = new PaginationVo<>();
        paginationVo.setRecords(commentVoList);
        paginationVo.setTotal(commentService.getCommentCount(ENTITY_TYPE_POST, commentDto.getPid()));
        paginationVo.setCurrentPage(commentDto.getCurrentPage());
        return Result.success().setData(paginationVo);
    }

    @Override
    public Result uploadImg(MultipartFile file) {
        if (file != null) {
            String imgUrl = ImgUtil.upload(port, fileName, file);
            return Result.success().setData(imgUrl);
        }
        return Result.fail().setMsg("图片不能为空");
    }

    //通过id获取文章
    @Override
    public Post getPostById(Integer targetId) {
        //转义
        Post post = postMapper.queryPostById(targetId);
        if (post == null) {
            return null;
        }
        post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
        post.setTitle(HtmlUtils.htmlUnescape(post.getTitle()));
        return post;
    }

    /**
     * 查看个人文章列表
     */
    @Override
    public PaginationVo<PostVo> listByUserId(int currentPage, int uid) {
        //起始位置
        int offset = (currentPage - 1) * 5;
        //查询文章
        List<Post> posts = postMapper.getPosts(uid, offset, 5);
        //文章总数目
        int total = postMapper.getUserPostsCount(uid);
        List<PostVo> postVos = new ArrayList<>();
        for (Post post : posts) {
            PostVo postVo = new PostVo();
            //查询文章点赞数量
            long likeCount = likeService.getPostLikeCount(post.getId());
            post.setLikeCount(likeCount);
            //转化日期格式
            String strDateFormat = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
            post.setCreateTimeStr(sdf.format(post.getCreateTime()));

            post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
            post.setTitle(HtmlUtils.htmlUnescape(post.getTitle()));

            postVo.setPost(post);
            postVos.add(postVo);
        }
        PaginationVo paginationVo = new PaginationVo<PostVo>();
        paginationVo.setCurrentPage(currentPage);

        paginationVo.setTotal(total);
        paginationVo.setPageSize(5);
        paginationVo.setRecords(postVos);
        return paginationVo;
    }

    //搜索文章内容+标题
    @Override
    public Result search(String message, int currentPage) {
        int offset = (currentPage - 1) * 5;
        int total = 0;
        //按顶置、发布时间排序
        List<Post> posts = postMapper.searchPost(message, offset);
        total = postMapper.countSearchPost(message);
        List<PostVo> postVos = getPostMessage(posts);
        PaginationVo paginationVo = new PaginationVo<PostVo>();
        paginationVo.setCurrentPage(currentPage);
        paginationVo.setTotal(total);
        paginationVo.setPageSize(5);
        paginationVo.setRecords(postVos);
        return Result.success().setData(paginationVo);
    }


    /**
     * 查询文章信息+作者信息
     *
     * @param posts
     * @return
     */
    public List<PostVo> getPostMessage(List<Post> posts) {
        List<PostVo> postVos = new ArrayList<>();
        for (Post post : posts) {
            PostVo postVo = new PostVo();
            //查询文章点赞数量
            long likeCount = likeService.getPostLikeCount(post.getId());
            post.setLikeCount(likeCount);
            //转化日期格式
            String strDateFormat = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
            post.setCreateTimeStr(sdf.format(post.getCreateTime()));
            //转义
            String content = HtmlUtils.htmlUnescape(post.getContent());
            String title = HtmlUtils.htmlUnescape(post.getTitle());
            post.setTitle(title);
            post.setContent(content);
            //简短内容
            if (content.length() > 50) {
                post.setContent(post.getContent().substring(0, 50) + "...");
            }
            postVo.setPost(post);
            //查询文章的作者
            User user = userService.getUserById(post.getUserId());
            postVo.setUser(user);
            postVos.add(postVo);
        }
        return postVos;
    }

    /**
     * 查询一级评论以及二级评论
     *
     * @param commentList
     * @param state
     * @param userId
     * @return
     */
    public List<CommentVo> getCommentVoList(List<Comment> commentList, boolean state, int userId) {
        List<CommentVo> commentVoList = new ArrayList<>();
        long likeCount = 0;
        int likeStatus = 0;
        if (commentList != null) {
            for (Comment comment : commentList) {
                CommentVo commentVo = new CommentVo();
                // 点赞数量
                likeCount = likeService.getCommentLikeCount(comment.getId());
                comment.setLikeCount(likeCount);
                // 点赞状态
                if (state) {
                    likeStatus = likeService.getCommentLikeStatus(userId, comment.getId());
                } else {
                    likeStatus = 0;
                }
                comment.setLikeStatus(likeStatus);
                //评论
                commentVo.setComment(comment);
                //作者
                commentVo.setUser(userService.getUserById(comment.getUserId()));
                //回复列表（二级评论）,全查
                List<Comment> replyList = commentService.getCommentsByEntity(
                        ConstantUtil.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复vo集合
                List<ReplyVo> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        ReplyVo replyVo = new ReplyVo();
                        // 点赞数量
                        likeCount = likeService.getCommentLikeCount(reply.getId());
                        reply.setLikeCount(likeCount);
                        // 点赞状态
                        likeStatus = state ? likeService.getCommentLikeStatus(userId, comment.getId()) : 0;
                        reply.setLikeStatus(likeStatus);
                        replyVo.setReply(reply);
                        replyVo.setUser(userService.getUserById(reply.getUserId()));
                        User target = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                        replyVo.setTarget(target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.setReplies(replyVoList);
                //回复(二级评论)数量
                commentVo.setReplyCount(replyVoList.size());
                commentVoList.add(commentVo);
            }
        }
        return commentVoList;
    }
}
