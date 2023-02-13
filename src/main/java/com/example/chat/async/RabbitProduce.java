package com.example.chat.async;

import com.alibaba.fastjson.JSON;
import com.example.chat.config.RabbitConfig;
import com.example.chat.pojo.Event;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 向消息队列中添加任务
 */
@Component
public class RabbitProduce {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void handleTask(Event event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_TOPIC_INFORM, event.getTopic(), JSON.toJSONBytes(event));
    }

}
