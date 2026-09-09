package com.statesmartlife.trust.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveIncidentTicketRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;

    private boolean approve;
}
