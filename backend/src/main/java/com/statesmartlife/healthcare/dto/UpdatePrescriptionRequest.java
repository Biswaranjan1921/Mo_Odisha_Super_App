package com.statesmartlife.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrescriptionRequest {

    @NotBlank(message = "Prescription notes are required")
    private String prescriptionNotes;

    private String prescriptionUrl;
}
