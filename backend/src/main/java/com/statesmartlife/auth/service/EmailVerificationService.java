package com.statesmartlife.auth.service;

import com.statesmartlife.auth.entity.EmailVerificationEntity;
import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.mail.EmailService;
import com.statesmartlife.auth.repository.EmailVerificationRepository;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final long expirationMs;

    public EmailVerificationService(
            EmailVerificationRepository verificationRepository,
            UserRepository userRepository,
            EmailService emailService,
            @Value("${app.auth.email-verification-expiration-ms:86400000}") long expirationMs) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.expirationMs = expirationMs;
    }

    public void sendVerificationToken(UserEntity user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        EmailVerificationEntity entity = EmailVerificationEntity.builder()
                .userId(user.getId())
                .verificationTokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(expirationMs))
                .used(false)
                .build();

        verificationRepository.save(entity);
        emailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    public void verifyEmailToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        EmailVerificationEntity verification = verificationRepository
                .findByVerificationTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new BusinessRuleException(
                        "INVALID_VERIFICATION_TOKEN",
                        "Invalid or already used verification token",
                        HttpStatus.BAD_REQUEST));

        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException(
                    "VERIFICATION_TOKEN_EXPIRED",
                    "Verification token has expired. Please request a new link",
                    HttpStatus.BAD_REQUEST);
        }

        verification.setUsed(true);
        verificationRepository.save(verification);

        UserEntity user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        user.setActive(true);
        userRepository.save(user);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
