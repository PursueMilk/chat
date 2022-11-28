package com.example.chat.utils;

import com.example.chat.mapper.UserMapper;
import com.example.chat.pojo.User;
import com.example.chat.vo.UserVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_TOKEN;

@SpringBootTest
public class QueryTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    @Test
    void queryUser() {
        User user = new User();
        user.setUsername("123");
        userMapper.queryUser(user);
    }

    @Test
    void existKey() {
        UserVo userVo = (UserVo) redisUtil.getUserVo(PREFIX_USER_TOKEN + "b5bb16a25b324369b64d8f7cdb35fed2");
        System.out.println(userVo);
    }
}
