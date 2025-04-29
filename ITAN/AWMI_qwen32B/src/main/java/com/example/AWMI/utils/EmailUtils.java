package com.example.AWMI.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

@Component
public class EmailUtils {
    @Resource
    private JavaMailSender mailSender;  // 移除 static

    private static final ConcurrentHashMap<String, String> codeMap = new ConcurrentHashMap<>();

    public String sendCode(String email) {  // 移除 static
        String code = generateCode();
        codeMap.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("896826234@qq.com");  // 这里要加上发件邮箱
        message.setTo(email);
        message.setSubject("【ITAN】您的注册验证码");
        message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");

        mailSender.send(message);
        return "验证码已发送";
    }

//    public String sendCode(String email) {
//        try {
//            // 打印调试信息
//            Properties props = new Properties();
//            props.put("mail.smtp.host", "smtp.qq.com");
//            props.put("mail.smtp.port", "465");
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.ssl.enable", "true");
//            Session session = Session.getInstance(props);
//            Transport transport = session.getTransport("smtp");
//            transport.connect("smtp.qq.com", 465, "896826234@qq.com", "mayiozbgxfzlbega");
//            System.out.println("SMTP 连接成功！");
//            transport.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "SMTP 连接失败：" + e.getMessage();
//        }
//
//        // 发送邮件逻辑
//        String code = generateCode();
//        codeMap.put(email, code);
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("896826234@qq.com");  // 发件人邮箱
//        message.setTo(email);
//        message.setSubject("【验证码】您的注册验证码");
//        message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");
//
//        mailSender.send(message);
//        return "验证码已发送";
//    }

    public boolean verifyCode(String email, String code) {  // 移除 static
        return codeMap.containsKey(email) && codeMap.get(email).equals(code);
    }

    private String generateCode() {  // 移除 static
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}
