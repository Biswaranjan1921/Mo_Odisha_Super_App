package com.statesmartlife.tourism.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyGuideRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String idProofUrl;

    @Min(value = 0, message = "Experience years must be >= 0")
    private int experienceYears;

    @NotBlank(message = "Languages spoken are required")
    private String languagesSpoken;
}
