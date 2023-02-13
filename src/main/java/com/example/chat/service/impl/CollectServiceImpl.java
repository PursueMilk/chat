package com.example.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.example.chat.mapper.PostMapper;
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

/**
 * 文章收藏实现
 */
@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostMapper postMapper;


    /**
     * 文章的被收藏数量
     */
    //TODO 收藏时间出现问题
    @Override
    public long getPostCollectCount(int id) {
        String postCollectKey = getPostCollect(id);
        Long collectCount = redisTemplate.opsForZSet().size(postCollectKey);
        return Objects.isNull(collectCount) ? 0 : collectCount.longValue();
    }

    /**
     * 查询用户是否收藏该文章
     */
    @Override
    public int getPostCollectStatus(int userId, int pid) {
        String userCollectKey = getUserCollect(userId);
        return Objects.isNull(redisTemplate.opsForZSet().score(userCollectKey, pid)) ? 0 : 1;
    }

    @Override
    public Result collect(int userId, int entityId) {
        //开启事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String postCollectKey = getPostCollect(entityId);
                String userCollectKey = getUserCollect(userId);
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
                String postCollectKey = getPostCollect(entityId);
                String userCollectKey = getUserCollect(userId);
                operations.multi();
                operations.opsForZSet().remove(userCollectKey, entityId);
                operations.opsForZSet().remove(postCollectKey, userId);
                return operations.exec();
            }
        });
        // 返回收藏数量
        long collectCount = getPostCollectCount(entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("collectCount", collectCount);
        map.put("collectStatus", 0);
        return Result.success().setData(map);
    }


    /**
     * 查看用户收藏的文章数量
     */
    @Override
    public long getUserCollectCount(int uid) {
        String userCollectKey = getUserCollect(uid);
        Long collectCount = redisTemplate.opsForZSet().zCard(userCollectKey);
        return Objects.isNull(collectCount) ? 0 : collectCount.longValue();
    }

    //查询用户收藏的文章
    @Override
    public List<Map<String, Object>> getCollections(int userId, int offset, int limit) {
        String userCollectKey = getUserCollect(userId);
        //获取文章ID
        Set<Integer> targetIds = redisTemplate.opsForZSet()
                .reverseRange(userCollectKey, offset, offset + limit - 1);
        if (Objects.isNull(targetIds)) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        //文章内容+收藏时间
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            Post post = postMapper.queryPostById(targetId);
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
