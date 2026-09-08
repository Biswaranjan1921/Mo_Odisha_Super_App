package com.statesmartlife.auth.mail;

public interface EmailService {
    void sendVerificationEmail(String recipientEmail, String rawVerificationToken);
}
