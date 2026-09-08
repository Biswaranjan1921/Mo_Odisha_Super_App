package com.statesmartlife.auth.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailpitEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MailpitEmailService.class);

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public MailpitEmailService(
            JavaMailSender mailSender,
            @Value("${app.auth.base-url:http://localhost:5173}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendVerificationEmail(String recipientEmail, String rawVerificationToken) {
        String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + rawVerificationToken;
        String subject = "State Smart Life — Mo Odisha Email Verification";
        String body = "Welcome to State Smart Life (Mo Odisha).\n\n"
                + "Please verify your email address by clicking the link below:\n"
                + verificationUrl + "\n\n"
                + "This verification link will expire in 24 hours.\n\n"
                + "Jay Jagannath!";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@statesmartlife.odisha.gov.in");
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Verification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.warn("SMTP server unreachable. Logging verification link for dev testing: {}", verificationUrl);
        }
    }
}
