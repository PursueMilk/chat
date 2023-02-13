package com.example.chat.interceptor;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.chat.annotion.TokenPass;
import com.example.chat.utils.RedisKeyUtil;
import com.example.chat.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    private RedisUtil redisUtil;

    public TokenInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = RedisKeyUtil.getUserTokenKey(request.getHeader("TOKEN"));
        //携带正确token访问就会刷新过期时间
        log.info("携带的token:{}", token);
        if (BooleanUtil.isTrue(redisUtil.existKey(token))) {
            redisUtil.refreshToken(token);
        }
        //判断访问接口、图片
        if (handler instanceof HandlerInterceptor) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            log.info("客户端请求访问{}", handlerMethod);
            TokenPass tokenPass = handlerMethod.getMethodAnnotation(TokenPass.class);
            //无须登录直接放行
            if (ObjectUtil.isNotNull(tokenPass)) {
                return true;
            } else if (BooleanUtil.isTrue(redisUtil.existKey(token))) {
                //token存在
                return true;
            }
            return false;
        }
        return true;
    }

}
