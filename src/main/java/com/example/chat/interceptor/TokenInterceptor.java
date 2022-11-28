package com.example.chat.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.example.chat.annotion.TokenPass;
import com.example.chat.utils.RedisKeyUtil;
import com.example.chat.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_TOKEN;


@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    private RedisUtil redisUtil;

    public TokenInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("TOKEN");
        //携带正确token访问就会刷新过期时间
        log.info("{}",token);
        if (!Objects.isNull(token) && BooleanUtil.isTrue(redisUtil.existKey(token))){
            String key = PREFIX_USER_TOKEN + token;
            redisUtil.refreshToken(key);
        }
        //判断访问接口、图片
        if (handler instanceof HandlerInterceptor) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            log.info("客户端请求访问{}", handlerMethod);
            TokenPass tokenPass = handlerMethod.getMethodAnnotation(TokenPass.class);
            //无须登录直接放行
            if (!Objects.isNull(tokenPass)) {
                return true;
            }
            //获取token
            if (!Objects.isNull(token) && BooleanUtil.isTrue(redisUtil.existKey(token))) {
                //token存在
                return true;
            }
            return false;
        }
        return true;
    }

}
