package com.example.chat.service.impl;

import com.example.chat.service.LikeService;
import com.example.chat.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import static com.example.chat.utils.ConstantUtil.ENTITY_TYPE_POST;
import static com.example.chat.utils.RedisKeyUtil.*;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //通过redis查询点赞
    @Override
    public Long getPostLikeCount(Integer postId) {
        String postLikeKey = PREFIX_POST_LIKE + postId;
        return redisTemplate.opsForSet().size(postLikeKey);
    }

    @Override
    public int getPostLikeStatus(int userId, int pid) {
        String postLikeKey = PREFIX_POST_LIKE + pid;
        return redisTemplate.opsForSet().isMember(postLikeKey, userId) ? 1 : 0;
    }

    @Override
    public long getCommentLikeCount(int id) {
        String commentLikeKey = PREFIX_COMMENT_LIKE + id;
        return redisTemplate.opsForSet().size(commentLikeKey);
    }

    @Override
    public int getCommentLikeStatus(int userId, int id) {
        String commentLikeKey = PREFIX_COMMENT_LIKE + id;
        return redisTemplate.opsForSet().isMember(commentLikeKey, userId) ? 1 : 0;
    }

    @Override
    public void like(Integer userId, String pre, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = pre + entityId;
                boolean isMember = operations.opsForSet().isMember(key, userId);
                //开启事务
                operations.multi();
                if (isMember) {
                    //取消点赞
                    operations.opsForSet().remove(key, userId);
                    operations.opsForValue().decrement(key);
                } else {
                    //点赞
                    operations.opsForSet().add(key, userId);
                    operations.opsForValue().increment(key);
                }
                //提交事务
                return operations.exec();
            }
        });
    }

    @Override
    public int getUserLikeCount(int userId) {
        String userLikeKey = PREFIX_USER_LIKE_TOTAL+userId;
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }


}
