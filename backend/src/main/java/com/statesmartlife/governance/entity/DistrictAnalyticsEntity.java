package com.statesmartlife.governance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tbl_district_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictAnalyticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "district_id", nullable = false, unique = true)
    private UUID districtId;

    @Column(name = "total_citizens", nullable = false)
    @Builder.Default
    private int totalCitizens = 0;

    @Column(name = "total_stores", nullable = false)
    @Builder.Default
    private int totalStores = 0;

    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private int totalOrders = 0;

    @Column(name = "total_revenue", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "active_sos_alerts", nullable = false)
    @Builder.Default
    private int activeSosAlerts = 0;

    @Column(name = "telehealth_bookings", nullable = false)
    @Builder.Default
    private int telehealthBookings = 0;

    @Column(name = "disputes_under_review", nullable = false)
    @Builder.Default
    private int disputesUnderReview = 0;

    @Column(name = "verified_guides", nullable = false)
    @Builder.Default
    private int verifiedGuides = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
