package com.statesmartlife.auth.service;

import com.statesmartlife.auth.dto.LoginRequest;
import com.statesmartlife.auth.dto.LoginResponse;
import com.statesmartlife.auth.dto.MessageResponse;
import com.statesmartlife.auth.dto.RefreshTokenRequest;
import com.statesmartlife.auth.dto.RegisterRequest;
import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.mail.EmailService;
import com.statesmartlife.auth.repository.EmailVerificationRepository;
import com.statesmartlife.auth.repository.RefreshTokenRepository;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.common.security.JwtProvider;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private EmailVerificationService emailVerificationService;
    private RefreshTokenService refreshTokenService;
    private AuthService authService;
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(
                "407f3f619e917d23d8c835a6104ab08cfb27429188e7f80ad22a0e2849be54c5",
                900000L,
                2592000000L
        );

        EmailService noopEmailService = (recipientEmail, rawVerificationToken) -> {
            // No-op for data JPA testing
        };

        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);

        emailVerificationService = new EmailVerificationService(
                emailVerificationRepository,
                userRepository,
                noopEmailService,
                86400000L
        );

        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                userRepository,
                jwtProvider,
                2592000000L
        );

        authService = new AuthService(
                userRepository,
                userProfileRepository,
                passwordEncoder,
                jwtProvider,
                emailVerificationService,
                refreshTokenService
        );
    }

    @Test
    @DisplayName("Registration should create INACTIVE user account and send verification email")
    void testRegistrationCreatesInactiveAccount() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Biswanath Patra")
                .phoneNumber("9876543210")
                .email("test.citizen@odisha.gov.in")
                .password("SecurePass123")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .role("CUSTOMER")
                .build();

        MessageResponse response = authService.register(request);

        assertTrue(response.isSuccess());

        Optional<UserEntity> savedUserOpt = userRepository.findByEmail("test.citizen@odisha.gov.in");
        assertTrue(savedUserOpt.isPresent());

        UserEntity savedUser = savedUserOpt.get();
        assertFalse(savedUser.isActive(), "Registered user must remain INACTIVE prior to email verification");
        assertEquals("CUSTOMER", savedUser.getRole());
    }

    @Test
    @DisplayName("Registration must reject public ADMIN role selection")
    void testRegistrationRejectsAdminRole() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Attacker Admin")
                .phoneNumber("9999999999")
                .email("attacker@admin.com")
                .password("Password123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role("ADMIN")
                .build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authService.register(request));
        assertEquals("INVALID_ROLE", ex.getErrorCode());
    }

    @Test
    @DisplayName("Unverified account login must be rejected with EMAIL_NOT_VERIFIED")
    void testUnverifiedAccountLoginRejected() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Unverified Resident")
                .phoneNumber("9811122233")
                .email("unverified@odisha.gov.in")
                .password("Password123")
                .dateOfBirth(LocalDate.of(1988, 3, 20))
                .role("CUSTOMER")
                .build();

        authService.register(request);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("unverified@odisha.gov.in")
                .password("Password123")
                .build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> authService.login(loginRequest));
        assertEquals("EMAIL_NOT_VERIFIED", ex.getErrorCode());
    }

    @Test
    @DisplayName("Verified account login should succeed and return access + refresh tokens")
    void testVerifiedAccountLoginSucceeds() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Verified Merchant")
                .phoneNumber("9877766655")
                .email("merchant@odisha.gov.in")
                .password("MerchantPass123")
                .dateOfBirth(LocalDate.of(1982, 8, 10))
                .role("SHOP_OWNER")
                .build();

        authService.register(request);

        UserEntity user = userRepository.findByEmail("merchant@odisha.gov.in").orElseThrow();
        user.setActive(true);
        userRepository.save(user);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("merchant@odisha.gov.in")
                .password("MerchantPass123")
                .build();

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("SHOP_OWNER", response.getRole());
        assertEquals(user.getId(), response.getUserId());
    }

    @Test
    @DisplayName("Refresh token rotation should revoke old token and issue new token pair")
    void testRefreshTokenRotationAndRevocation() {
        UserEntity user = UserEntity.builder()
                .phoneNumber("9123456789")
                .email("courier@odisha.gov.in")
                .passwordHash("hashed")
                .role("DELIVERY_PARTNER")
                .active(true)
                .build();

        UserEntity savedUser = userRepository.save(user);

        String initialRefreshToken = refreshTokenService.createRefreshToken(savedUser);
        assertNotNull(initialRefreshToken);

        // Rotate token
        LoginResponse rotatedResponse = refreshTokenService.rotateRefreshToken(initialRefreshToken);
        assertNotNull(rotatedResponse.getAccessToken());
        assertNotNull(rotatedResponse.getRefreshToken());
        assertNotEquals(initialRefreshToken, rotatedResponse.getRefreshToken());

        // Attempting to reuse initial revoked refresh token must be rejected
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> refreshTokenService.rotateRefreshToken(initialRefreshToken));
        assertEquals("REFRESH_TOKEN_INVALID", ex.getErrorCode());
    }
}
