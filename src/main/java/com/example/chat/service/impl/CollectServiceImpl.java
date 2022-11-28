package com.example.chat.service.impl;

import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.service.CollectService;
import com.example.chat.service.PostService;
import com.example.chat.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.chat.utils.RedisKeyUtil.*;

@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostService postService;

    /**
     * 获得文章的收藏数量
     * @param id
     * @return
     */
    @Override
    public long getPostCollectCount(int id) {
        String postCollectKey = PREFIX_POST_COLLECT + id;
        return redisTemplate.opsForZSet().zCard(postCollectKey);
    }

    /**
     * 查询用户是否收藏该文章
     * @param userId
     * @param pid
     * @return
     */
    @Override
    public int getPostCollectStatus(int userId, int pid) {
        String userCollectKey = PREFIX_USER_COLLECT + userId;
        return redisTemplate.opsForZSet().score(userCollectKey, pid) == null ? 0 : 1;
    }

    @Override
    public Result collect(int userId, int entityId) {
        //开启事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String postCollectKey = PREFIX_POST_COLLECT + entityId;
                String userCollectKey = PREFIX_USER_COLLECT + userId;
                operations.multi();
                //放入有序集合
                operations.opsForZSet().add(userCollectKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(postCollectKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
        // 返回文章被收藏数量
        long collectCount = getPostCollectCount(entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("collectCount", collectCount);
        map.put("collectStatus", 1);
        return Result.success().setData(map);
    }

    @Override
    public Result unCollect(int userId, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String postCollectKey = PREFIX_POST_COLLECT + entityId;
                String userCollectKey = PREFIX_USER_COLLECT + userId;

                operations.multi();

                operations.opsForZSet().remove(userCollectKey, entityId);
                operations.opsForZSet().remove(postCollectKey, userId);

                return operations.exec();
            }
        });
        // 返回粉丝数量
        long collectCount = getPostCollectCount(entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("collectCount", collectCount);
        map.put("collectStatus", 0);
        return Result.success().setData(map);
    }


    @Override
    public long getUserCollectCount(int uid) {
        String userCollectKey = PREFIX_USER_COLLECT + uid;
        return redisTemplate.opsForZSet().zCard(userCollectKey);
    }

    //查询用户收藏的文章
    @Override
    public List<Map<String, Object>> getCollections(int userId, int offset, int limit) {
        String userCollectKey = PREFIX_USER_COLLECT + userId;
        //获取文章ID
        Set<Integer> targetIds = redisTemplate
                .opsForZSet()
                .reverseRange(userCollectKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            Post post = postService.getPostById(targetId);
            map.put("post", post);
            Double score = redisTemplate.opsForZSet().score(userCollectKey, targetId);
            //转化日期格式
            String strDateFormat = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
            Date date = new Date(score.longValue());
            map.put("collectTime", sdf.format(date));
            list.add(map);
        }
        return list;
    }


}
