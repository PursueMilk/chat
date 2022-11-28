package com.example.chat.controller;


import com.example.chat.async.EventHandler;
import com.example.chat.pojo.Comment;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.service.CommentService;
import com.example.chat.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.example.chat.utils.ConstantUtil.*;


@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private EventHandler eventHandler;

    /**
     * 评论
     *
     * @param comment
     * @return
     */
    @PostMapping("/add")
    public Result comment(@RequestBody Comment comment) {
        int userId = getUserId();
        comment.setUserId(userId);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);
        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(userId)
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", comment.getPostId())
                .setData("content", comment.getContent());
        if (comment.getEntityType() == ENTITY_TYPE_POST) { //评论帖子
            Post target = postService.getPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) { //评论的对象也是评论
            Comment target = commentService.getCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        //TODO 异步发送事件(用线程池,发送回复通知)
        eventHandler.handleTask(event);
        //如果评论了帖子，则需要更新es库中评论数量
/*        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(comment.getUserId());
            //用线程池异步
            eventHandler.handleTask(event);*/
        // 增加分数
        redisUtil.increaseScore(comment.getPostId(), 10);
        return Result.success();
    }

}

