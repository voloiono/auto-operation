package com.example.autooperation.service;

import com.example.autooperation.config.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final SettingsService settingsService;

    public void sendExecutionNotification(String toEmail, String flowName, boolean success, String output, String errorMessage) {
        try {
            Map<String, String> emailSettings = settingsService.getGroup("email");

            if (emailSettings.isEmpty() || emailSettings.getOrDefault("email.host", "").isBlank()) {
                log.warn("Email settings not configured, skipping notification");
                return;
            }

            JavaMailSenderImpl mailSender = EmailConfig.createMailSender(emailSettings);
            String fromEmail = emailSettings.getOrDefault("email.fromEmail", emailSettings.getOrDefault("email.username", "noreply@autooperation.com"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Automation Script Execution: " + flowName + " - " + (success ? "SUCCESS" : "FAILED"));

            StringBuilder body = new StringBuilder();
            body.append("Flow: ").append(flowName).append("\n");
            body.append("Status: ").append(success ? "SUCCESS" : "FAILED").append("\n");
            body.append("Timestamp: ").append(java.time.LocalDateTime.now()).append("\n\n");

            if (success) {
                body.append("Output:\n").append(output);
            } else {
                body.append("Error:\n").append(errorMessage);
            }

            message.setText(body.toString());
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}", toEmail, e);
        }
    }
}
