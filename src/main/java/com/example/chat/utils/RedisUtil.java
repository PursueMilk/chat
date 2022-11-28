package com.example.chat.utils;

import cn.hutool.core.util.BooleanUtil;
import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.PREDIX_POST_SCORE;

@Component
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 刷新token
     * @param key
     * @return
     */
    public Boolean refreshToken(String key) {
        if (BooleanUtil.isTrue(existKey(key))) {
            redisTemplate.expire(key, 2, TimeUnit.HOURS);
            return true;
        }
        return false;
    }

    /**
     * 设置邮箱验证码
     * @param key
     * @param code
     */
    public void setCode(String key, String code) {
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }


    /**
     * 确保key存在
     * @param key
     * @return
     */
    public Boolean existKey(String key) {
        return redisTemplate.hasKey(key);
    }


    public int getId(String uuidKey) {
        UserVo userVo = getUserVo(uuidKey);
        return userVo.getId();
    }


    public UserVo getUserVo(String uuidKey){
        return (UserVo) redisTemplate.opsForValue().get(uuidKey);
    }


    public void setPostScore(String key,Integer postId){
        redisTemplate.opsForZSet().add(key,postId,0);
    }

    public void increaseScore(Integer postId,double score){
        Double num=redisTemplate.opsForZSet().score(PREDIX_POST_SCORE,postId);
        redisTemplate.opsForZSet().add(PREDIX_POST_SCORE,postId,num+score);
    }

}
