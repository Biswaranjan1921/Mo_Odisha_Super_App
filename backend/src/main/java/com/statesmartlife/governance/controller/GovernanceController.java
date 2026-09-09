package com.statesmartlife.governance.controller;

import com.statesmartlife.governance.dto.DistrictAnalyticsResponse;
import com.statesmartlife.governance.dto.StateOverviewResponse;
import com.statesmartlife.governance.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/governance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GovernanceController {

    private final GovernanceService governanceService;

    @GetMapping("/analytics/overview")
    public ResponseEntity<StateOverviewResponse> getStateOverview() {
        return ResponseEntity.ok(governanceService.getStateOverview());
    }

    @GetMapping("/analytics/districts")
    public ResponseEntity<List<DistrictAnalyticsResponse>> getDistrictAnalytics() {
        return ResponseEntity.ok(governanceService.getDistrictAnalytics());
    }
}
