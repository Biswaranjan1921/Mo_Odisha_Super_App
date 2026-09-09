package com.statesmartlife.emergency.dto;

import com.statesmartlife.emergency.enums.EmergencyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmergencyStatusRequest {

    @NotNull(message = "Target emergency status is required")
    private EmergencyStatus status;

    private String dispatcherNotes;
}
