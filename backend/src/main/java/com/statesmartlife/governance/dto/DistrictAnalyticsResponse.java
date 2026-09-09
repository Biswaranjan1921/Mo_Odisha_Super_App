package com.statesmartlife.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictAnalyticsResponse {
    private UUID districtId;
    private String districtCode;
    private String districtName;
    private int totalCitizens;
    private int totalStores;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private int activeSosAlerts;
    private int telehealthBookings;
    private int disputesUnderReview;
    private int verifiedGuides;
}
