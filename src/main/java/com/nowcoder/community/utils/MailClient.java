package com.nowcoder.community.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    // 注入JavaMailSender，Spring Boot 会通过spring-boot-starter-mail自动配置JavaMailSender Bean，无需手动定义
    @Autowired
    private JavaMailSender mailSender;

    // 发件人，value注解读取到spring.mail.username并注入到from中
    @Value("${spring.mail.username}")
    private String from;

    // 封装发邮件的逻辑
    public void sendMail(String to, String subject, String content) {
        try {
            // 模版对象（空的）
            MimeMessage message = mailSender.createMimeMessage();
            // 利用Sring提供的MimeMessageHelper，构建message里面的内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            // 发送出去 也可以写为mailSender.send(message);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }

        }
    }
