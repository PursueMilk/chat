package com.example.chat.chat;


import com.example.chat.dto.MessageDto;
import com.example.chat.pojo.Message;
import com.example.chat.service.MessageService;
import com.example.chat.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//configurator = SpringConfigurator.class意思是让spring组件可以注入
@ServerEndpoint(value = "/chat/{token}")
@Component
@Slf4j
public class ChatEndpoint {

    @Autowired
    private RedisUtil redisUtil;

    //用来存储每一个客户端对象对应的ChatEndpoint对象
    private static Map<String, ChatEndpoint> onlineUsers = new ConcurrentHashMap<>();

    public static MessageService messageService;

    //和某个客户端连接对象，需要通过他来给客户端发送数据
    private Session session;
    private String userId;

    @OnOpen
    //连接建立成功调用
    public void onOpen(@PathParam("token") String token, Session session, EndpointConfig config) throws IOException {

        if (!redisUtil.existKey(token)) {
            return;
        }
        //需要通知其他的客户端，将所有的用户的用户名发送给客户端
        this.session = session;
        userId = String.valueOf(redisUtil.getId(token));
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


    private Set<String> getNames() {
        return onlineUsers.keySet();
    }

    @OnMessage
    //接收到消息时调用
    public void onMessage(String message, Session session) {
        try {
            //获取客户端发送来的数据  {"toName":"张三","message":"你好"}
            ObjectMapper mapper = new ObjectMapper();
            MessageDto messageDto = mapper.readValue(message, MessageDto.class);
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
            //录入到数据库，逻辑存在错误
            messageService.addMessage(dbMessage);
            ChatEndpoint chatEndpoint = onlineUsers.get(messageDto.getToId());
            //对方不在线
            if (chatEndpoint == null) return;
            //将数据推送给指定的客户端
            chatEndpoint.session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    //连接关闭时调用
    public void onClose(Session session) {
        //移除连接对象
        onlineUsers.remove(userId);
        System.out.println(userId + " 退出连接啦~~~ ");
        System.out.println("在线总人数： " + onlineUsers.size());
    }
}
