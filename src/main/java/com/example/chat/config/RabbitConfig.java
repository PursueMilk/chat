package com.example.chat.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    //主题交换机
    public static final String EXCHANGE_TOPIC_INFORM = "exchange_topic_inform";

    //通知队列的绑定路由
    public static final String BINDING_MESSAGE = "inform.message.*";

    //邮件队列的绑定路由
    public static final String BINDING_EMAIL = "inform.email.*";

    public static final String BINDING_ES = "inform.es.*";

    //通知处理队列
    public static final String QUEUE_INFORM_MESSAGE = "queue_inform_message";

    //邮件处理队列
    public static final String QUEUE_INFORM_EMAIL = "queue_inform_mail";

    public static final String QUEUE_INFORM_ES = "queue_inform_es";


    @Bean(EXCHANGE_TOPIC_INFORM)
    public Exchange getExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPIC_INFORM).durable(true).build();
    }

    @Bean(QUEUE_INFORM_MESSAGE)
    public Queue getMessageQueue() {
        return new Queue(QUEUE_INFORM_MESSAGE);
    }

    @Bean(QUEUE_INFORM_EMAIL)
    public Queue getEmailQueue() {
        return new Queue(QUEUE_INFORM_EMAIL);
    }

    @Bean(QUEUE_INFORM_ES)
    public Queue getESQueue() {
        return new Queue(QUEUE_INFORM_ES);
    }

    @Bean
    public Binding bindingEmailQueue(@Qualifier(QUEUE_INFORM_EMAIL) Queue queue,
                                     @Qualifier(EXCHANGE_TOPIC_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BINDING_EMAIL).noargs();
    }

    @Bean
    public Binding bindingMessageQueue(@Qualifier(QUEUE_INFORM_MESSAGE) Queue queue,
                                       @Qualifier(EXCHANGE_TOPIC_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BINDING_MESSAGE).noargs();
    }

    @Bean
    public Binding bindingESQueue(@Qualifier(QUEUE_INFORM_ES) Queue queue,
                                       @Qualifier(EXCHANGE_TOPIC_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BINDING_ES).noargs();
    }

}
