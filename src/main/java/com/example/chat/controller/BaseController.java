package com.example.chat.controller;

import cn.hutool.core.util.BooleanUtil;
import com.example.chat.utils.RedisUtil;
import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;

import static com.example.chat.utils.RedisKeyUtil.getUserTokenKey;

@Controller
public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected RedisUtil redisUtil;


    public Integer getUserId() {
        return getUserVo().getId();
    }

    public UserVo getUserVo(){
        String token = request.getHeader("TOKEN");
        return redisUtil.getUserVo(getUserTokenKey(token));
    }

    public boolean isLogin() {
        String token = request.getHeader("TOKEN");
        return BooleanUtil.isTrue(redisUtil.existKey(getUserTokenKey(token)));
    }

}
