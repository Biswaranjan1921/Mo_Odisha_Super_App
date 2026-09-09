package com.statesmartlife.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateOverviewResponse implements Serializable {
    private long totalCitizens;
    private long totalStores;
    private long totalOrders;
    private BigDecimal totalCommerceRevenue;
    private long activeSosEmergencies;
    private long telehealthBookings;
    private long disputesUnderReview;
    private long verifiedTourGuides;
}
