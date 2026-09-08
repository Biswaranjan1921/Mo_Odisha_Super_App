package com.statesmartlife.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private final String secretKey = "407f3f619e917d23d8c835a6104ab08cfb27429188e7f80ad22a0e2849be54c5";
    private final long expirationMs = 900000; // 15 mins
    private final long refreshExpirationMs = 2592000000L; // 30 days

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(secretKey, expirationMs, refreshExpirationMs);
    }

    @Test
    @DisplayName("Should generate valid access token and extract correct claims")
    void testCreateAccessTokenAndExtractClaims() {
        String userId = "usr_9982736152";
        String email = "citizen@odisha.gov.in";
        String role = "CUSTOMER";

        String token = jwtProvider.createAccessToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals(userId, jwtProvider.extractUserId(token));
        assertEquals(email, jwtProvider.extractEmail(token));
        assertEquals(role, jwtProvider.extractRole(token));
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testCreateRefreshToken() {
        String userId = "usr_9982736152";

        String token = jwtProvider.createRefreshToken(userId);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
        assertEquals(userId, jwtProvider.extractUserId(token));
    }

    @Test
    @DisplayName("Should reject invalid or malformed token")
    void testInvalidTokenValidation() {
        String malformedToken = "invalid.jwt.token.string";
        assertFalse(jwtProvider.validateToken(malformedToken));
    }
}
