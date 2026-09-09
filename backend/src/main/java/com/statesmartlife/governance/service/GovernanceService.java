package com.statesmartlife.governance.service;

import com.statesmartlife.governance.dto.DistrictAnalyticsResponse;
import com.statesmartlife.governance.dto.StateOverviewResponse;

import java.util.List;

public interface GovernanceService {
    StateOverviewResponse getStateOverview();
    List<DistrictAnalyticsResponse> getDistrictAnalytics();
}
