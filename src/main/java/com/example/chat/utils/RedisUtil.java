package com.example.chat.utils;

import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.PREDIX_POST_SCORE;

@Component
public class RedisUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 刷新token
     * @param key
     * @return
     */
    public void refreshToken(String key) {
        redisTemplate.expire(key, 6, TimeUnit.HOURS);
    }

    /**
     * 设置邮箱验证码
     * @param key
     * @param code
     */
    public void setCode(String key, String code) {
        redisTemplate.opsForValue().set(key, code, 10, TimeUnit.MINUTES);
    }


    /**
     * 判断Key是否存在
     * @param key
     * @return
     */
    public Boolean existKey(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 获取用户id
     * @param token
     * @return
     */
    public int getId(String token) {
        UserVo userVo = getUserVo(token);
        return userVo.getId();
    }


    /**
     * 获取缓存的用户信息
     * @param token
     * @return
     */
    public UserVo getUserVo(String token) {
        return (UserVo) redisTemplate.opsForValue().get(token);
    }


    /**
     * 初始化文章分数
     * @param key
     * @param postId
     */
    public void setPostScore(String key, Integer postId) {
        redisTemplate.opsForZSet().add(key, postId, 0);
    }



    /**
     * 查询热度文章
     * @param offset
     * @param limit
     * @return
     */
    public Set<Integer> getHotPostId(int offset,int limit){
        Set<Integer> targetIds = redisTemplate
                .opsForZSet()
                .reverseRange(PREDIX_POST_SCORE, offset, offset + limit - 1);
        return targetIds;
    }


    /**
     * 帖子分数
     * @param postId
     * @param score
     */
    public void increaseScore(Integer postId, double score) {
        Double num = redisTemplate.opsForZSet().score(PREDIX_POST_SCORE, postId);
        double number = Objects.isNull(num) ? 0 : num;
        redisTemplate.opsForZSet().add(PREDIX_POST_SCORE, postId, number + score);
    }

}
