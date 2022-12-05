package com.example.chat.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Slf4j
@Component
public class MailUtil {

    @Autowired
    private JavaMailSender sender;

    @Value("${server.port}")
    private String port;

    private static final String subject = "校园论坛";

    public void sendMail(String from, String to, String content) throws MessagingException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setSubject(subject);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSentDate(new Date());
        helper.setText(content);
        sender.send(mimeMessage);
    }


    //TODO 修改页面
    public void mailRegister(String from, String to, String code, String account) throws MessagingException {
        String httpUrl = ImgUtil.getHttpUrl() + port + "/user/activation";
        String content = "您已注册校园社区，快来点击连接激活吧！\n" +
                httpUrl + "?code=" + code + "&account=" + account +
                "\n如果不是您注册，请不要点击激活连接";
        log.info("邮箱内容{}", content);
         sendMail(from,to,content);
    }


    public void mailUpdatePasswd(String from, String to, String passwd, String account) throws MessagingException {
        String content = "尊敬的 " + account + " , 您的新密码是：" + passwd;
        log.info("新密码：", passwd);
        sendMail(from, to, content);
    }
}
