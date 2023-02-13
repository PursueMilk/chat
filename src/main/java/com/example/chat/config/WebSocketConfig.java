package com.example.chat.config;

import com.example.chat.chat.ChatEndpoint;
import com.example.chat.mapper.MessageMapper;
import com.example.chat.service.MessageService;
import com.example.chat.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    @Bean
    //注入ServerEndpointExporter，自动注册使用@ServerEndpoint注解的
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Autowired
    public void setMessageService(MessageMapper messageMapper, RedisUtil redisUtil) {
        ChatEndpoint.messageMapper = messageMapper;
        ChatEndpoint.redisUtil = redisUtil;
    }
}
