package com.statesmartlife.auth.service;

import com.statesmartlife.auth.dto.LoginRequest;
import com.statesmartlife.auth.dto.LoginResponse;
import com.statesmartlife.auth.dto.MessageResponse;
import com.statesmartlife.auth.dto.RefreshTokenRequest;
import com.statesmartlife.auth.dto.RegisterRequest;
import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.common.security.JwtProvider;
import com.statesmartlife.user.entity.UserProfileEntity;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            EmailVerificationService emailVerificationService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailVerificationService = emailVerificationService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Security Rule: Public registration MUST NOT select ADMIN
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BusinessRuleException(
                    "INVALID_ROLE",
                    "Public registration for ADMIN role is strictly forbidden",
                    HttpStatus.FORBIDDEN);
        }

        // Validate Role against allowed public onboarding roles
        String uppercaseRole = request.getRole().toUpperCase();
        if (!isAllowedRole(uppercaseRole)) {
            throw new BusinessRuleException(
                    "INVALID_ROLE",
                    "Invalid registration role specified: " + request.getRole(),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException(
                    "EMAIL_ALREADY_REGISTERED",
                    "An account with this email address already exists",
                    HttpStatus.CONFLICT);
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessRuleException(
                    "PHONE_ALREADY_REGISTERED",
                    "An account with this phone number already exists",
                    HttpStatus.CONFLICT);
        }

        // Create inactive user
        UserEntity user = UserEntity.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(uppercaseRole)
                .active(false)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // Save initial user profile with date_of_birth
        UserProfileEntity profile = UserProfileEntity.builder()
                .userId(savedUser.getId())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        userProfileRepository.save(profile);

        // Generate verification token and dispatch email
        emailVerificationService.sendVerificationToken(savedUser);

        return MessageResponse.builder()
                .success(true)
                .message("Registration successful. Please check your email to activate your account.")
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessRuleException(
                        "INVALID_CREDENTIALS",
                        "Invalid email or password",
                        HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException(
                    "INVALID_CREDENTIALS",
                    "Invalid email or password",
                    HttpStatus.UNAUTHORIZED);
        }

        // Security Rule: Reject unverified accounts
        if (!user.isActive()) {
            throw new BusinessRuleException(
                    "EMAIL_NOT_VERIFIED",
                    "Your email address has not been verified. Please check your inbox",
                    HttpStatus.FORBIDDEN);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId().toString(), user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public MessageResponse verifyEmail(String token) {
        emailVerificationService.verifyEmailToken(token);
        return MessageResponse.builder()
                .success(true)
                .message("Email verified successfully. Your account is now active.")
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.rotateRefreshToken(request.getRefreshToken());
    }

    public MessageResponse logout(RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return MessageResponse.builder()
                .success(true)
                .message("Logged out successfully.")
                .build();
    }

    private boolean isAllowedRole(String role) {
        return "CUSTOMER".equals(role)
                || "SHOP_OWNER".equals(role)
                || "DELIVERY_PARTNER".equals(role)
                || "HEALTHCARE_PROVIDER".equals(role);
    }
}
