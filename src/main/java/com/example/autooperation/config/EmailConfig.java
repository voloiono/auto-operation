package com.example.autooperation.config;

import com.example.autooperation.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

@Configuration
@Slf4j
public class EmailConfig {

    /**
     * 创建一个默认的 JavaMailSender bean（空配置）。
     * 实际发送邮件时通过 DynamicMailService 从数据库读取最新配置创建 sender。
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        return mailSender;
    }

    /**
     * 根据数据库配置创建一个新的 JavaMailSenderImpl
     */
    public static JavaMailSenderImpl createMailSender(Map<String, String> emailSettings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailSettings.getOrDefault("email.host", "smtp.gmail.com"));
        mailSender.setPort(Integer.parseInt(emailSettings.getOrDefault("email.port", "587")));
        mailSender.setUsername(emailSettings.getOrDefault("email.username", ""));
        mailSender.setPassword(emailSettings.getOrDefault("email.password", ""));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        boolean enableTls = "true".equals(emailSettings.getOrDefault("email.enableTls", "true"));
        props.put("mail.smtp.starttls.enable", String.valueOf(enableTls));
        props.put("mail.smtp.starttls.required", String.valueOf(enableTls));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}
