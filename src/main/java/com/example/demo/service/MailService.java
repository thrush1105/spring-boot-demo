package com.example.demo.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {

    private final MailSender mailSender;

    @Value("${mail.from:}")
    private String from;

    @Value("${mail.to:}")
    private String to;

    public MailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public CompletableFuture<Void> sendTestMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("テストメール");
        message.setText("Spring Bootによるメール送信のテストです。");

        log.debug("Started sending email: " + message.toString());
        mailSender.send(message);
        log.debug("Finished sending email: " + message.toString());

        return CompletableFuture.completedFuture(null);
    }

}
