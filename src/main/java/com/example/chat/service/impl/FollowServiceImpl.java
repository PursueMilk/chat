package com.example.chat.service.impl;

import com.example.chat.pojo.User;
import com.example.chat.service.FollowService;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_FOLLOW;
import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_FOLLOWED;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public void follow(Integer userId, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = PREFIX_USER_FOLLOW + userId;
                String followedKey = PREFIX_USER_FOLLOWED + entityId;
                //开启事务
                operations.multi();
                //往用户的关注集合中添加被关注人的id
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                //往被关注人的粉丝集合中添加粉丝id
                operations.opsForZSet().add(followedKey, userId, System.currentTimeMillis());
                //执行事务
                return operations.exec();
            }
        });
    }

    @Override
    public long getFollowerCount(int entityId) {
        String followerKey = PREFIX_USER_FOLLOWED + entityId;
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public void unfollow(Integer id, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = PREFIX_USER_FOLLOW + id;
                String followedKey = PREFIX_USER_FOLLOWED + entityId;
                //开启事务
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followedKey, id);
                return operations.exec();
            }
        });
    }

    @Override
    public long getFolloweeCount(int userId) {
        String followerKey = PREFIX_USER_FOLLOW + userId;
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    @Override
    public boolean hasFollowed(int userId, int entityId) {
        String followeeKey = PREFIX_USER_FOLLOW + userId;
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    @Override
    public List<Map<String, Object>> getFollowees(int userId, int offset, int limit) {
        String followeeKey = PREFIX_USER_FOLLOW + userId;
        Set<Integer> targetIds = redisTemplate.opsForZSet()
                .reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        //用户关注的人及时间
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //TODO 优化
    @Override
    public List<Map<String, Object>> getFollowers(int userId, int offset, int limit) {
        String followerKey = PREFIX_USER_FOLLOWED + userId;
        Set<Integer> targetIds = redisTemplate.opsForZSet()
                .reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getUserById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            //关注的时间
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
