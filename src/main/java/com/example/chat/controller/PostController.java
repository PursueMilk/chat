package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.CommentDto;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.SearchResult;
import com.example.chat.service.ElasticSearchService;
import com.example.chat.service.LikeService;
import com.example.chat.service.PostService;
import com.example.chat.service.UserService;
import com.example.chat.vo.PaginationVo;
import com.example.chat.vo.PostVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@Api(tags = "文章接口")
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @ApiOperation(value = "发表文章")
    @PostMapping("/publish")
    public Result publish(@RequestBody Post post) {
        post.setUserId(getUserId());
        return postService.publish(post);
    }


    @ApiOperation(value = "首页文章列表")
    @TokenPass
    @GetMapping("/list")
    public Result posts(@RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "0") int listMode) {
        PaginationVo<PostVo> pagination = postService.posts(currentPage, listMode);
        return Result.success().setData(pagination);
    }


    @ApiOperation(value = "文章详情")
    @TokenPass
    @GetMapping("/detail/{pid}")
    public Result postDetail(@PathVariable("pid") int pid) {
        //判断是否登录
        return isLogin() ? postService.postDetail(pid, getUserId(), true) : postService.postDetail(pid, -1, false);
    }


    @ApiOperation(value = "评论列表")
    @TokenPass
    @PostMapping("/comment/list")
    public Result CommentList(@RequestBody CommentDto commentDto) {
        return isLogin() ? postService.CommentList(commentDto, getUserId(), true) : postService.CommentList(commentDto, -1, false);
    }


    @ApiOperation(value = "上传图片")
    @PostMapping("/uploadImg")
    public Result uploadImg(@RequestPart MultipartFile file) {
        return postService.uploadImg(file);
    }


    @TokenPass
    @ApiOperation("查询信息")
    @GetMapping("/search")
    public Result searchPost(@RequestParam("keyword") String message, @RequestParam(defaultValue = "1") int currentPage) {
        System.out.println(message);
        SearchResult searchResult = null;
        List<PostVo> postVos = null;
        try {
            searchResult = elasticSearchService.searchPost(message, (currentPage - 1) * 5, 5);
            postVos = new ArrayList<>();
            List<Post> list = searchResult.getList();
            if (list != null) {
                for (Post post : list) {
                    PostVo vo = new PostVo();
                    //转化日期格式
                    String strDateFormat = "yyyy-MM-dd HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
                    post.setCreateTimeStr(sdf.format(post.getCreateTime()));
                    // 点赞数目
                    post.setLikeCount(likeService.getPostLikeCount(post.getId()));
                    //帖子
                    vo.setPost(post);
                    //作者
                    vo.setUser(userService.getUserById(post.getUserId()));
                    postVos.add(vo);
                }
            }
        } catch (IOException e) {
            log.error("系统出错，没有数据：" + e.getMessage());
        }
        PaginationVo paginationVo = new PaginationVo<PostVo>();
        paginationVo.setCurrentPage(currentPage);
        paginationVo.setTotal(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());
        paginationVo.setPageSize(5);
        paginationVo.setRecords(postVos);
        System.out.println(paginationVo);
        return Result.success().setData(paginationVo);
    }


    @TokenPass
    @ApiOperation("查询信息")
    @GetMapping("/search2")
    public Result searchPost2(@RequestParam("keyword") String message, @RequestParam(defaultValue = "1") int currentPage) {
        System.out.println(message);
        return postService.search("%" + message + "%", currentPage);
    }
}
