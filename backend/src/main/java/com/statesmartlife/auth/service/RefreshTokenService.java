package com.statesmartlife.auth.service;

import com.statesmartlife.auth.dto.LoginResponse;
import com.statesmartlife.auth.entity.RefreshTokenEntity;
import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.RefreshTokenRepository;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.common.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtProvider jwtProvider,
            @Value("${app.jwt.refresh-expiration-ms:2592000000}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public String createRefreshToken(UserEntity user) {
        String rawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return rawRefreshToken;
    }

    @Transactional
    public LoginResponse rotateRefreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);

        RefreshTokenEntity tokenEntity = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new BusinessRuleException(
                        "REFRESH_TOKEN_INVALID",
                        "Invalid or revoked refresh token",
                        HttpStatus.UNAUTHORIZED));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            tokenEntity.setRevoked(true);
            refreshTokenRepository.save(tokenEntity);
            throw new BusinessRuleException(
                    "REFRESH_TOKEN_EXPIRED",
                    "Refresh token has expired. Please log in again",
                    HttpStatus.UNAUTHORIZED);
        }

        // Revoke existing refresh token (Token Rotation)
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new BusinessRuleException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessRuleException("EMAIL_NOT_VERIFIED", "Account is not verified", HttpStatus.FORBIDDEN);
        }

        // Issue new access token and new refresh token
        String newAccessToken = jwtProvider.createAccessToken(user.getId().toString(), user.getEmail(), user.getRole());
        String newRefreshToken = createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
            return;
        }
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(entity -> {
                    entity.setRevoked(true);
                    refreshTokenRepository.save(entity);
                });
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
