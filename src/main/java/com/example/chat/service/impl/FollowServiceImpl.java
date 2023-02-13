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

import static com.example.chat.utils.RedisKeyUtil.*;

/**
 * 用户关注实现
 */
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注
     */
    @Override
    public void follow(Integer userId, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = getUserFollow(userId);
                String fansKey = getUserFans(entityId);
                //开启事务
                operations.multi();
                //往用户的关注集合中添加被关注人的id
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                //往被关注人的粉丝集合中添加粉丝id
                operations.opsForZSet().add(fansKey, userId, System.currentTimeMillis());
                //执行事务
                return operations.exec();
            }
        });
    }


    /**
     * 取消关注
     */
    @Override
    public void unfollow(Integer id, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = getUserFollow(id);
                String fansKey = getUserFans(entityId);
                //开启事务
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(fansKey, id);
                return operations.exec();
            }
        });
    }


    /**
     * 获取粉丝数量
     */
    @Override
    public long getFansCount(int entityId) {
        String fansKey = getUserFans(entityId);
        Long fansCount = redisTemplate.opsForZSet().zCard(fansKey);
        return Objects.isNull(fansCount) ? 0 : fansCount.longValue();
    }


    /**
     * 关注数量
     */
    @Override
    public long getFolloweeCount(int userId) {
        String followerKey = getUserFollow(userId);
        Long followCount = redisTemplate.opsForZSet().zCard(followerKey);
        return Objects.isNull(followCount) ? 0 : followCount.intValue();
    }


    /**
     * 判断是否关注
     */
    @Override
    public int hasFollowed(int userId, int entityId) {
        String followeeKey = getUserFollow(userId);
        return Objects.isNull(redisTemplate.opsForZSet().score(followeeKey, entityId)) ? 0 : 1;
    }

    /**
     * 关注列表
     */
    @Override
    public List<Map<String, Object>> getFollowees(int userId, int offset, int limit) {
        String followeeKey = getUserFollow(userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet()
                .reverseRange(followeeKey, offset, offset + limit - 1);
        if (Objects.isNull(targetIds)) {
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


    /**
     * 粉丝列表
     */
    @Override
    public List<Map<String, Object>> getFans(int userId, int offset, int limit) {
        String followerKey = getUserFans(userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet()
                .reverseRange(followerKey, offset, offset + limit - 1);
        if (Objects.isNull(targetIds)) {
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
