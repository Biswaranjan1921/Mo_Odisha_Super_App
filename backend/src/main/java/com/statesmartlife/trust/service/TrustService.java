package com.statesmartlife.trust.service;

import com.statesmartlife.trust.dto.FileIncidentTicketRequest;
import com.statesmartlife.trust.dto.IncidentTicketResponse;
import com.statesmartlife.trust.dto.ResolveIncidentTicketRequest;
import com.statesmartlife.trust.dto.TrustScoreResponse;
import com.statesmartlife.trust.enums.EntityType;
import com.statesmartlife.trust.enums.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface TrustService {

    TrustScoreResponse getTrustScore(UUID entityId, EntityType entityType);

    TrustScoreResponse recordVerifiedTransaction(UUID entityId, EntityType entityType);

    IncidentTicketResponse fileIncidentTicket(UUID reporterId, FileIncidentTicketRequest request);

    List<IncidentTicketResponse> getMyIncidentTickets(UUID reporterId);

    List<IncidentTicketResponse> getAllIncidentTickets();

    IncidentTicketResponse updateTicketStatus(UUID ticketId, TicketStatus newStatus, UUID adminId);

    IncidentTicketResponse resolveIncidentTicket(UUID ticketId, ResolveIncidentTicketRequest request, UUID adminId);
}
