package com.example.chat.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.example.chat.pojo.PostScore;
import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.example.chat.utils.RedisKeyUtil.*;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 刷新token
     *
     * @param key
     * @return
     */
    public void refreshToken(String key) {
        redisTemplate.expire(key, 6, TimeUnit.HOURS);
    }

    /**
     * 设置邮箱验证码
     *
     * @param key
     * @param code
     */
    public void setCode(String key, String code) {
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }


    /**
     * 判断Key是否存在
     *
     * @param key
     * @return
     */
    public Boolean existKey(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 获取用户id
     *
     * @param token
     * @return
     */
    public int getId(String token) {
        UserVo userVo = getUserVo(token);
        return userVo.getId();
    }


    /**
     * 获取缓存的用户信息
     *
     * @param token
     * @return
     */
    public UserVo getUserVo(String token) {
        return (UserVo) redisTemplate.opsForValue().get(token);
    }


    /**
     * 查询热度文章
     *
     * @param offset
     * @param limit
     * @return
     */
    public Set<Integer> getHotPostId(int offset, int limit) {
        Set<Integer> targetIds = redisTemplate
                .opsForZSet()
                .reverseRange(POST_SCORE, offset, offset + limit - 1);
        return targetIds;
    }


    public void setHotList(String key, List<PostScore> list){
       for (PostScore postScore:list){
           redisTemplate.opsForZSet().add(key,postScore.getId(),postScore.getScore());
       }
    }


    /**
     * 插入改变队列
     *
     * @param postId
     */
    public void insertChange(Integer postId) {
        redisTemplate.opsForSet().add(POST_CHANGE, postId);
    }


    /**
     * 构建缓存
     */
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从redis查询商铺缓存
        String post = (String) redisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(post)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(post, type);
        }
        // 判断命中的是否是空值
        if (post != null) {
            // 返回一个错误信息
            return null;
        }

        // 4.实现缓存重建
        // 4.1.获取互斥锁
        String lockKey = LOCK_KEY + key;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2.判断是否获取成功
            if (!isLock) {
                // 4.3.获取锁失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4.4.获取锁成功，根据id查询数据库
            r = dbFallback.apply(id);
            // 5.不存在，返回错误
            if (r == null) {
                // 将空值写入redis
                redisTemplate.opsForValue().set(key, "", 10, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }
            // 6.存在，写入redis
            redisTemplate.opsForValue().set(key, JSON.toJSONString(r), time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7.释放锁
            unlock(lockKey);
        }
        // 8.返回
        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        redisTemplate.delete(key);
    }

}
