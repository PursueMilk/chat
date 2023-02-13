package com.example.chat.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.example.chat.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.example.chat.utils.RedisKeyUtil.*;

/**
 * 点赞实现
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 文章的点赞数量
     */
    @Override
    public long getPostLikeCount(int postId) {
        String postLikeKey = getPostLike(postId);
        Long likeCount = redisTemplate.opsForSet().size(postLikeKey);
        return Objects.isNull(likeCount) ? 0 : likeCount.longValue();
    }

    /**
     * 文章的赞状态
     */
    @Override
    public int getPostLikeStatus(int userId, int pid) {
        String postLikeKey = getPostLike(pid);
        return BooleanUtil.isTrue(redisTemplate.opsForSet().isMember(postLikeKey, userId)) ? 1 : 0;
    }

    /**
     * 评论的点赞数量
     */
    @Override
    public long getCommentLikeCount(int id) {
        String commentLikeKey = getCommentLike(id);
        Long commentCount = redisTemplate.opsForSet().size(commentLikeKey);
        return Objects.isNull(commentCount) ? 0 : commentCount.longValue();
    }

    /**
     * 评论的点赞状态
     */
    @Override
    public int getCommentLikeStatus(int userId, int id) {
        String commentLikeKey = getCommentLike(id);
        return BooleanUtil.isTrue(redisTemplate.opsForSet().isMember(commentLikeKey, userId)) ? 1 : 0;
    }

    /**
     * 点赞和取消点赞
     */
    @Override
    public void like(Integer userId, String pre, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String postKey = pre + entityId;
                String userKey = PREFIX_USER_LIKE_TOTAL + entityUserId;
                Boolean isMember = operations.opsForSet().isMember(postKey, userId);
                //开启事务
                operations.multi();
                if (BooleanUtil.isTrue(isMember)) {
                    //取消点赞
                    operations.opsForSet().remove(postKey, userId);
                    operations.opsForValue().decrement(userKey);
                } else {
                    //点赞
                    operations.opsForSet().add(postKey, userId);
                    operations.opsForValue().increment(userKey);
                }
                //提交事务
                return operations.exec();
            }
        });
    }





    /**
     * 用户获得的总点赞数
     */
    @Override
    public int getUserLikeCount(int userId) {
        String userLikeKey = getUserLikeTotal(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return Objects.isNull(count) ? 0 : count.intValue();
    }


}
