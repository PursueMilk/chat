package com.example.chat.controller;


import com.example.chat.async.RabbitProduce;
import com.example.chat.pojo.Comment;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.service.CommentService;
import com.example.chat.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.example.chat.utils.ConstantUtil.*;


@Api(tags = "评论接口")
@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private RabbitProduce rabbitProduce;


    @ApiOperation(value = "添加评论")
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
            redisUtil.insertChange(comment.getEntityId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) { //评论的对象也是评论
            Comment target = commentService.getCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        rabbitProduce.handleTask(event);
        return Result.success();
    }

}

