package com.statesmartlife.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Baseline System Health and Information Controller.
 * Provides system status, API versioning info, and active runtime environment details.
 */
@Tag(name = "System", description = "System health, versioning, and status metrics APIs")
@RestController
@RequestMapping("/system")
public class SystemHealthController {

    @Value("${spring.application.name:state-smart-life}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Operation(summary = "Get system runtime status", description = "Returns system operational status, active profile, version, and server timestamp.")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("application", appName);
        info.put("version", "1.0.0");
        info.put("profile", activeProfile);
        info.put("timestamp", Instant.now().toString());
        info.put("systemMessage", "State Smart Life — Mo Odisha Super App Foundation Ready");
        return ResponseEntity.ok(info);
    }
}
