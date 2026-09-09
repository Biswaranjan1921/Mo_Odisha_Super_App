package com.statesmartlife.tourism.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuideProfileRequest {

    private String professionalHeadline;
    private String aboutMe;
    private String profileImageUrl;
    private String coverImageUrl;
    private String languagesSpoken;

    @Min(value = 0, message = "Experience years must be >= 0")
    private int experienceYears;

    private String specialization;

    @DecimalMin(value = "0.00", message = "Hourly rate must be >= 0")
    private BigDecimal hourlyRate;

    private Boolean isAvailable;
}
