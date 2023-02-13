package com.example.chat.controller;


import com.example.chat.async.RabbitProduce;
import com.example.chat.dto.LikeDto;
import com.example.chat.limit.RateEnum;
import com.example.chat.limit.RateLimit;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.LikeService;
import com.example.chat.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.example.chat.utils.ConstantUtil.ENTITY_TYPE_POST;
import static com.example.chat.utils.ConstantUtil.TOPIC_LIKE;
import static com.example.chat.utils.RedisKeyUtil.PREFIX_COMMENT_LIKE;
import static com.example.chat.utils.RedisKeyUtil.PREFIX_POST_LIKE;

@Api(tags = "点赞接口")
@RestController
public class LikeController extends BaseController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
    @Autowired
    private RabbitProduce rabbitProduce;


    @ApiOperation(value = "点赞接口")
    @PostMapping("/like")
    @RateLimit(rate = RateEnum.RATE_5000_PER_SECONDS)
    public Result like(@RequestBody LikeDto likeDto) {
        int userId = getUserId();
        User user = userService.getUserById(userId);
        // 点赞
        String preKey = likeDto.getEntityType() == ENTITY_TYPE_POST ? PREFIX_POST_LIKE : PREFIX_COMMENT_LIKE;
        likeService.like(user.getId(), preKey, likeDto.getEntityId(), likeDto.getEntityUserId());
        long likeCount = 0;
        int likeStatus = 0;
        // 数量
        if (likeDto.getEntityType() == ENTITY_TYPE_POST) {
            likeCount = likeService.getPostLikeCount(likeDto.getEntityId());
            likeStatus = likeService.getPostLikeStatus(user.getId(), likeDto.getEntityId());
        } else {
            likeCount = likeService.getCommentLikeCount(likeDto.getEntityId());
            likeStatus = likeService.getCommentLikeStatus(user.getId(), likeDto.getEntityId());
        }
        // 状态:已点赞还是未点赞,1代表已点赞，0代表未点赞
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);
        map.put("entityType", likeDto.getEntityType());
        // 触发点赞事件,发送通知
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(userId)
                    .setEntityType(likeDto.getEntityType())
                    .setEntityId(likeDto.getEntityId())
                    .setEntityUserId(likeDto.getEntityUserId())
                    .setData("postId", likeDto.getPostId());
            rabbitProduce.handleTask(event);
        }
        //点赞的是帖子
        if (likeDto.getEntityType() == ENTITY_TYPE_POST) {
            // 插入修改
            redisUtil.insertChange(likeDto.getPostId());
        }
        return Result.success().setData(map);
    }
}
