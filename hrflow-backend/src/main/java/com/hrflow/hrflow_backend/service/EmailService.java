package com.hrflow.hrflow_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("HRFlow - Verify your email");
        message.setText(
                "Hello,\n\n" +
                        "Please verify your email by clicking the link below:\n\n" +
                        "http://localhost:4200/verify-email?token=" + token + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "HRFlow Team"
        );
        javaMailSender.send(message);
    }
}
