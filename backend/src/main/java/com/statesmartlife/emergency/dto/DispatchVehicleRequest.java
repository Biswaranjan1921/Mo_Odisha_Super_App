package com.statesmartlife.emergency.dto;

import com.statesmartlife.emergency.enums.ResponseServiceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchVehicleRequest {

    @NotNull(message = "Response service type is required")
    private ResponseServiceType responseService;

    private UUID assignedResponderId;

    private String dispatcherNotes;
}
