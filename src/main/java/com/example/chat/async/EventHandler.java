package com.example.chat.async;

import com.example.chat.pojo.Event;
import com.example.chat.utils.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步处理任务
 */
@Component
public class EventHandler {

    @Autowired
    private EmailTask emailTask;

    @Autowired
    private MessageTask messageTask;

    private static ExecutorService executorService;

    private static Set<String> messageTopics = new HashSet<>();
    private static Set<String> emailTopics = new HashSet<>();

    static {
        //TODO 优化线程池，添加现场数量+rabbitmq
        executorService = Executors.newFixedThreadPool(4);
        //发送邮件的任务
        emailTopics.add(ConstantUtil.TOPIC_REGISTER);
        emailTopics.add(ConstantUtil.TOPIC_FORGET);
        //发送消息任务
        messageTopics.add(ConstantUtil.TOPIC_COMMENT);
        messageTopics.add(ConstantUtil.TOPIC_LIKE);
        messageTopics.add(ConstantUtil.TOPIC_FOLLOW);
    }

    public void handleTask(Event event) {
        if (messageTopics.contains(event.getTopic())) {
            //通知任务
            messageTask.setEvent(event);
            executorService.submit(messageTask);
        } else if (emailTopics.contains(event.getTopic())) {
            //邮件任务
            emailTask.setEvent(event);
            executorService.execute(emailTask);
        }
    }


}
