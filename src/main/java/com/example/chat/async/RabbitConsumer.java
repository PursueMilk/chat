package com.example.chat.async;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.chat.config.RabbitConfig;
import com.example.chat.mapper.*;
import com.example.chat.pojo.*;
import com.example.chat.service.ElasticSearchService;
import com.example.chat.utils.ConstantUtil;
import com.example.chat.utils.MailUtil;
import com.example.chat.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.chat.utils.ConstantUtil.*;
import static com.example.chat.utils.RedisKeyUtil.getUserCodeKey;
import static com.example.chat.utils.RedisKeyUtil.getUserEmailKey;


/**
 * 消息队列执行任务
 */
@Slf4j
@Component
public class RabbitConsumer {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @RabbitListener(queues = RabbitConfig.QUEUE_INFORM_MESSAGE)
    public void messageHandler(String bytes) {
        Event event= JSON.parseObject(bytes,Event.class);
        log.info("任务主题：" + event.getTopic());
        //自己不通知自己
        if (event.getUserId() == event.getEntityUserId()) return;
        message(event);
    }


    public void message(Event event) {
        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        //发送给被点赞、关注的用户
        message.setToId(event.getEntityUserId());
        //点赞、关注、评论
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        //通知内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        //把data的数据放入消息内容
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        //存入数据库
        messageMapper.insertMessage(message);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_INFORM_ES)
    public void esHandler(byte[] bytes) {
        Event event= JSON.parseObject(bytes,Event.class);
        Post post = postMapper.queryPostById(event.getEntityId());
        elasticSearchService.savePost(post);
    }


    @RabbitListener(queues = RabbitConfig.QUEUE_INFORM_EMAIL)
    public void emailHandler(byte[] bytes) {
        Event event= JSON.parseObject(bytes,Event.class);
        log.info("{}",event);
        if (ConstantUtil.TOPIC_REGISTER.equals(event.getTopic())) {
            register(event);
        } else if (ConstantUtil.TOPIC_FORGET.equals(event.getTopic())) {
            forget(event);
        }
    }


    /**
     * 注册
     */
    private void register(Event event) {
        JSONObject json = (JSONObject) event.getData().get("user");
        User user =  json.toJavaObject(User.class);
        //6位的验证码（数字+字符）
        String code = RandomUtil.randomString(6);
        try {
            mailUtil.mailRegister(user.getEmail(), code, user.getAccount());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        redisUtil.setCode(getUserCodeKey(user.getAccount()), code);
    }


    private void forget(Event event) {
        JSONObject json = (JSONObject) event.getData().get("user");
        User user =  json.toJavaObject(User.class);
        try {
            mailUtil.mailUpdatePasswd(user.getEmail(), user.getPasswd(), user.getAccount());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        redisUtil.setCode(getUserEmailKey(user.getEmail()), "exist");
    }

}
