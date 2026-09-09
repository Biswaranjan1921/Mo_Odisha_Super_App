package com.statesmartlife.trust.dto;

import com.statesmartlife.trust.enums.EntityType;
import com.statesmartlife.trust.enums.TicketStatus;
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
public class IncidentTicketResponse {
    private UUID id;
    private UUID reporterId;
    private UUID targetEntityId;
    private EntityType targetEntityType;
    private String subject;
    private String description;
    private TicketStatus status;
    private String resolutionNotes;
    private UUID resolvedBy;
    private Instant createdAt;
    private Instant resolvedAt;
    private Instant updatedAt;
}
