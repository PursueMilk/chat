package com.example.chat.async;

import com.alibaba.fastjson.JSONObject;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Message;
import com.example.chat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.chat.utils.ConstantUtil.SYSTEM_USER_ID;

/*
*
 * 发送通知的线程
 */

@Component
@Scope("prototype")
@Slf4j
public class MessageTask implements Runnable {

    private Event event;

    @Autowired
    private MessageService messageService;

    public void setEvent(Event event){
        this.event = event;
    }

    @Override
    public void run() {
        log.info("任务主题："+event.getTopic());
        //自己不通知自己
        if (event.getUserId() == event.getEntityUserId())return;
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
        messageService.addMessage(message);
    }
}
