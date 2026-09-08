package com.statesmartlife.user.controller;

import com.statesmartlife.common.config.OpenApiConfig;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.user.dto.UpdateUserProfileRequest;
import com.statesmartlife.user.dto.UserProfileResponse;
import com.statesmartlife.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "User Profile", description = "Authenticated user profile management & DOB accessibility engine APIs")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current authenticated user profile", description = "Retrieves profile, server-calculated age, elderly status, and UI accessibility payload for the authenticated security context.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UUID userId = extractAuthenticatedUserId();
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current user profile", description = "Updates profile details for authenticated user and recalculates server-side DOB age engine.")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UUID userId = extractAuthenticatedUserId();
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get citizen dashboard summary", description = "Retrieves aggregated citizen dashboard metrics, active services status, and system notices.")
    @GetMapping("/dashboard-summary")
    public ResponseEntity<com.statesmartlife.user.dto.DashboardSummaryResponse> getDashboardSummary() {
        UUID userId = extractAuthenticatedUserId();
        com.statesmartlife.user.dto.DashboardSummaryResponse response = userService.getDashboardSummary(userId);
        return ResponseEntity.ok(response);
    }

    private UUID extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            throw new BusinessRuleException("UNAUTHORIZED", "User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("INVALID_USER_ID", "Invalid user security principal", HttpStatus.UNAUTHORIZED);
        }
    }
}
