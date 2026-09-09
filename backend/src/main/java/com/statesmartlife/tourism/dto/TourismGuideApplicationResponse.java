package com.statesmartlife.tourism.dto;

import com.statesmartlife.tourism.enums.GuideApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourismGuideApplicationResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String licenseNumber;
    private String idProofUrl;
    private int experienceYears;
    private String languagesSpoken;
    private GuideApplicationStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
