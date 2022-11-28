package com.example.chat.controller;


import com.example.chat.async.EventHandler;
import com.example.chat.dto.LikeDto;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.LikeService;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.example.chat.utils.ConstantUtil.ENTITY_TYPE_POST;
import static com.example.chat.utils.ConstantUtil.TOPIC_LIKE;
import static com.example.chat.utils.RedisKeyUtil.*;


@RestController
public class LikeController extends BaseController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EventHandler eventHandler;


    /**
     * 点赞或取消点赞
     *
     * @param likeDto
     * @return
     */
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public Result like(@RequestBody LikeDto likeDto) {
        int userId = getUserId();
        User user = userService.getUserById(userId);
        // 点赞
        String preKey=likeDto.getEntityType()==ENTITY_TYPE_POST?PREFIX_COMMENT_LIKE:PREFIX_POST_LIKE;
        likeService.like(user.getId(),preKey, likeDto.getEntityId(), likeDto.getEntityUserId());
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
            eventHandler.handleTask(event);
        }
        //点赞的是帖子
        if (likeDto.getEntityType() == ENTITY_TYPE_POST) {
            // 计算帖子分数
            Double num = redisTemplate.opsForZSet().score(PREDIX_POST_SCORE, likeDto.getPostId());
            redisTemplate.opsForZSet().add(PREDIX_POST_SCORE, likeDto.getPostId(), num + 5);
        }
        return Result.success().setData(map);
    }


}
