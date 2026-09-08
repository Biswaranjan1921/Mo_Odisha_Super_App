package com.statesmartlife.auth.controller;

import com.statesmartlife.auth.dto.LoginRequest;
import com.statesmartlife.auth.dto.LoginResponse;
import com.statesmartlife.auth.dto.MessageResponse;
import com.statesmartlife.auth.dto.RefreshTokenRequest;
import com.statesmartlife.auth.dto.RegisterRequest;
import com.statesmartlife.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "User registration, email verification, login, token refresh, and logout APIs")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register user account", description = "Registers a new inactive user account and sends an email verification token.")
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Verify user email", description = "Validates cryptographic token hash and activates user account.")
    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User login", description = "Authenticates credentials and returns JWT access and refresh tokens.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token", description = "Rotates refresh token and issues new JWT access token.")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User logout", description = "Revokes the user refresh token.")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        String token = request != null ? request.getRefreshToken() : null;
        MessageResponse response = authService.logout(new RefreshTokenRequest(token));
        return ResponseEntity.ok(response);
    }
}
