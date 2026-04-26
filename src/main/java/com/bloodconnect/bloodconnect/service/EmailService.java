package com.bloodconnect.bloodconnect.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML

            mailSender.send(message);
            log.info("action=send_email_success to=\"{}\" subject=\"{}\"", to, subject);
        } catch (MessagingException e) {
            log.error("action=send_email_failed to=\"{}\" subject=\"{}\" error=\"{}\"", to, subject, e.getMessage());
        } catch (Exception e) {
            log.error("action=send_email_unexpected_failed to=\"{}\" subject=\"{}\" error=\"{}\"", to, subject, e.getMessage());
        }
    }
}
