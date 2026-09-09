package com.statesmartlife.trust.dto;

import com.statesmartlife.trust.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileIncidentTicketRequest {

    @NotNull(message = "Target entity ID is required")
    private UUID targetEntityId;

    @NotNull(message = "Target entity type is required")
    private EntityType targetEntityType;

    @NotBlank(message = "Subject is required")
    @Size(max = 150, message = "Subject must not exceed 150 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;
}
