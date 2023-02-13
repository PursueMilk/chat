package com.example.chat.chat;


import cn.hutool.core.util.BooleanUtil;
import com.example.chat.dto.MessageDto;
import com.example.chat.mapper.MessageMapper;
import com.example.chat.pojo.Message;
import com.example.chat.service.MessageService;
import com.example.chat.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.chat.utils.RedisKeyUtil.getUserTokenKey;


/**
 * 论坛成员私信实现
 */
@ServerEndpoint("/chat/{token}")
@Component
@Slf4j
public class ChatEndpoint {


    //用来存储每一个客户端对象对应的ChatEndpoint对象
    private static Map<String, ChatEndpoint> onlineUsers = new ConcurrentHashMap<>();

    public static MessageMapper messageMapper;

    public static RedisUtil redisUtil;


    //和某个客户端连接对象，需要通过他来给客户端发送数据
    private Session session;

    private String userId;

    @OnOpen
    //连接建立成功调用
    public void onOpen(@PathParam("token") String token, Session session) throws IOException {
        System.out.println(token);
        String tokenKey = getUserTokenKey(token);
        Boolean exist = redisUtil.existKey(tokenKey);
        if (BooleanUtil.isFalse(exist)) {
            return;
        }
        //需要通知其他的客户端，将所有的用户的用户名发送给客户端
        this.session = session;
        this.userId = String.valueOf(redisUtil.getId(tokenKey));
        //获取用户名
        //存储该链接对象,如果之前该用户就已经有过连接了，就关闭该链接
        if (onlineUsers.get(userId) != null) {
            onlineUsers.get(userId).session.close();
        }
        onlineUsers.put(userId, this);
        log.info("{}连接服务器啦~~~", userId);
        log.info("在线总人数：{}" + onlineUsers.size());
    }

    private void broadcastAllUsers(String message) {
        try {
            //遍历 onlineUsers 集合
            Set<String> names = onlineUsers.keySet();
            for (String name : names) {
                //获取该用户对应的ChatEndpoint对象
                ChatEndpoint chatEndpoint = onlineUsers.get(name);
                //发送消息
                chatEndpoint.session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //接收到消息时调用
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            //获取客户端发送来的数据  {"toName":"张三","message":"你好"}
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(message);
            MessageDto messageDto = mapper.readValue(message, MessageDto.class);
            log.info("消息记录{}", messageDto);
            int from_id = Integer.parseInt(messageDto.getFromId());
            int to_id = Integer.parseInt(messageDto.getToId());
            String conversation_id;
            if (from_id < to_id) {
                conversation_id = from_id + "_" + to_id;
            } else {
                conversation_id = to_id + "_" + from_id;
            }
            Message dbMessage = new Message();
            dbMessage.setConversationId(conversation_id);
            dbMessage.setFromId(from_id);
            dbMessage.setToId(to_id);
            dbMessage.setCreateTime(messageDto.getCreateTime());
            dbMessage.setContent(messageDto.getContent());
            //录入到数据库
            messageMapper.insertMessage(dbMessage);
            ChatEndpoint chatEndpoint = onlineUsers.get(messageDto.getToId());
            log.info("{}", chatEndpoint);
            //对方不在线
            if (Objects.isNull(chatEndpoint)) {
                return;
            }
            //将数据推送给指定的客户端
            chatEndpoint.session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    //连接关闭时调用
    public void onClose() {
        //移除连接对象
        onlineUsers.remove(userId);
        log.info("{} 退出连接啦~~~ ", userId);
        log.info("在线总人数： {}", onlineUsers.size());
    }
}
