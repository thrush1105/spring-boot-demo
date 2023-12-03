package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final MailSender mailSender;

    @Value("${mail.from:}")
    private String from;

    @Value("${mail.to:}")
    private String to;

    public MailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("テストメール");
        message.setText("Spring Bootによるメール送信のテストです。");
        mailSender.send(message);
    }

}
