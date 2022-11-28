package com.example.chat.async;

import cn.hutool.core.util.RandomUtil;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.User;
import com.example.chat.utils.ConstantUtil;
import com.example.chat.utils.MailUtil;
import com.example.chat.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_CODE;


/**
 * 发送邮件的线程
 */
@Component
@Scope("prototype")
public class EmailTask implements Runnable {

    private Event event;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private RedisUtil redisUtil;


    @Value("${spring.mail.username}")
    private String from;

    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * 注册
     */
    private void register() {
        Map<String, Object> data = event.getData();
        User user = (User) data.get("user");
        String code = RandomUtil.randomString(6);
        try {
            mailUtil.mailRegister(from, user.getEmail(), code, user.getUsername());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        redisUtil.setCode(PREFIX_USER_CODE + user.getUsername(), code);
    }

    /**
     * TODO 忘记密码
     */
/*    private void forget() {
        Map<String, Object> data = event.getData();
        User user = (User)data.get("user");
        //发送邮件
        try {
            mailUtil.forgetMail(user.getEmail(),"忘记密码",user);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }*/
    @Override
    public void run() {
        if (ConstantUtil.TOPIC_REGISTER.equals(event.getTopic())) {
            register();
        } else if (ConstantUtil.TOPIC_FORGET.equals(event.getTopic())) {
            /*           forget();*/
        }
    }
}
