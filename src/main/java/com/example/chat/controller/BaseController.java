package com.example.chat.controller;

import cn.hutool.core.util.BooleanUtil;
import com.example.chat.utils.RedisUtil;
import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_TOKEN;

@Controller
public class BaseController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    protected RedisUtil redisUtil;


    public int getUserId() {
        return getUserVo().getId();
    }

    public UserVo getUserVo(){
        String uuid = request.getHeader("TOKEN");
        return redisUtil.getUserVo(PREFIX_USER_TOKEN+uuid);
    }

    public boolean isLogin() {
        String token = request.getHeader("TOKEN");
        return BooleanUtil.isTrue(redisUtil.existKey(token));
    }

}
