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

    @Value("${img.ip}")
    private String ip;

    @Value("${server.port}")
    private String port;

    @Value("${spring.mail.username}")
    private String from;

    private static final String subject = "校园论坛";

    /**
     * 发送邮件
     * @param to
     * @param content
     * @throws MessagingException
     */
    public void sendMail(String to, String content) throws MessagingException {
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setSubject(subject);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSentDate(new Date());
        //设置为true能识别html文本
        helper.setText(content, true);
        sender.send(mimeMessage);
    }


    /**
     * 注册账号，发送激活信息
     * @param to
     * @param code
     * @param account
     * @throws MessagingException
     */
    public void mailRegister( String to, String code, String account) throws MessagingException {
        String httpUrl = ImgUtil.getImgUrl(ip, port) + "/user/activation";
        String content = "您已注册校园社区，快来点击连接激活吧！\n" +
                httpUrl + "?code=" + code + "&account=" + account +
                "\n如果不是您注册，请不要点击激活连接";
        log.info("邮箱内容{}", content);
        sendMail(to, content);
    }


    public void mailUpdatePasswd( String to, String passwd, String account) throws MessagingException {
        String content = "尊敬的 " + account + " , 您的新密码是：" + passwd;
        log.info("新密码：", passwd);
        sendMail(to, content);
    }
}
