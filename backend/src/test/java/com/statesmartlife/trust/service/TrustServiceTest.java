package com.statesmartlife.trust.service;

import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.core.exceptions.ResourceNotFoundException;
import com.statesmartlife.healthcare.repository.DoctorRepository;
import com.statesmartlife.trust.dto.FileIncidentTicketRequest;
import com.statesmartlife.trust.dto.IncidentTicketResponse;
import com.statesmartlife.trust.dto.ResolveIncidentTicketRequest;
import com.statesmartlife.trust.dto.TrustScoreResponse;
import com.statesmartlife.trust.entity.IncidentTicketEntity;
import com.statesmartlife.trust.entity.TrustScoreEntity;
import com.statesmartlife.trust.enums.BadgeLevel;
import com.statesmartlife.trust.enums.EntityType;
import com.statesmartlife.trust.enums.TicketStatus;
import com.statesmartlife.trust.exception.IncidentTicketNotFoundException;
import com.statesmartlife.trust.exception.InvalidDisputeTransitionException;
import com.statesmartlife.trust.repository.IncidentTicketRepository;
import com.statesmartlife.trust.repository.TrustScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrustServiceTest {

    private TrustScoreRepository trustScoreRepository;
    private IncidentTicketRepository incidentTicketRepository;
    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private DoctorRepository doctorRepository;
    private TrustServiceImpl trustService;

    private UUID storeId;
    private UUID reporterId;
    private UUID adminId;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        trustScoreRepository = mock(TrustScoreRepository.class);
        incidentTicketRepository = mock(IncidentTicketRepository.class);
        storeRepository = mock(StoreRepository.class);
        userRepository = mock(UserRepository.class);
        doctorRepository = mock(DoctorRepository.class);

        trustService = new TrustServiceImpl(
                trustScoreRepository,
                incidentTicketRepository,
                storeRepository,
                userRepository,
                doctorRepository
        );

        storeId = UUID.randomUUID();
        reporterId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getTrustScore - Creates default 100.00 PLATINUM score if entity score does not exist")
    void getTrustScore_Success() {
        when(storeRepository.existsById(storeId)).thenReturn(true);
        when(trustScoreRepository.findByEntityIdAndEntityType(storeId, EntityType.STORE)).thenReturn(Optional.empty());
        when(trustScoreRepository.save(any(TrustScoreEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrustScoreResponse response = trustService.getTrustScore(storeId, EntityType.STORE);

        assertNotNull(response);
        assertEquals(storeId, response.getEntityId());
        assertEquals(EntityType.STORE, response.getEntityType());
        assertEquals(new BigDecimal("100.00"), response.getTrustScore());
        assertEquals(BadgeLevel.PLATINUM, response.getBadgeLevel());
    }

    @Test
    @DisplayName("getTrustScore - Throws ResourceNotFoundException if entity does not exist")
    void getTrustScore_EntityNotFound_ThrowsException() {
        when(storeRepository.existsById(storeId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> trustService.getTrustScore(storeId, EntityType.STORE));
    }

    @Test
    @DisplayName("recordVerifiedTransaction - Increments transaction count and keeps score capped at 100.00")
    void recordVerifiedTransaction_Success() {
        when(storeRepository.existsById(storeId)).thenReturn(true);
        TrustScoreEntity existing = TrustScoreEntity.builder()
                .id(UUID.randomUUID())
                .entityId(storeId)
                .entityType(EntityType.STORE)
                .verifiedTransactionsCount(10)
                .complaintsCount(0)
                .trustScore(new BigDecimal("100.00"))
                .badgeLevel(BadgeLevel.PLATINUM)
                .build();
        when(trustScoreRepository.findByEntityIdAndEntityType(storeId, EntityType.STORE)).thenReturn(Optional.of(existing));
        when(trustScoreRepository.save(any(TrustScoreEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrustScoreResponse response = trustService.recordVerifiedTransaction(storeId, EntityType.STORE);

        assertEquals(11, response.getVerifiedTransactionsCount());
        assertEquals(new BigDecimal("100.00"), response.getTrustScore());
    }

    @Test
    @DisplayName("fileIncidentTicket - Files ticket, increments complaints, degrades trust score and badge level")
    void fileIncidentTicket_Success() {
        when(storeRepository.existsById(storeId)).thenReturn(true);

        TrustScoreEntity trustScoreEntity = TrustScoreEntity.builder()
                .id(UUID.randomUUID())
                .entityId(storeId)
                .entityType(EntityType.STORE)
                .verifiedTransactionsCount(0)
                .complaintsCount(1) // 1 previous complaint (score 98.50)
                .trustScore(new BigDecimal("98.50"))
                .badgeLevel(BadgeLevel.PLATINUM)
                .build();

        when(trustScoreRepository.findByEntityIdAndEntityType(storeId, EntityType.STORE)).thenReturn(Optional.of(trustScoreEntity));

        IncidentTicketEntity createdTicket = IncidentTicketEntity.builder()
                .id(ticketId)
                .reporterId(reporterId)
                .targetEntityId(storeId)
                .targetEntityType(EntityType.STORE)
                .subject("Defective Product")
                .description("Product was broken upon delivery")
                .status(TicketStatus.OPEN)
                .createdAt(Instant.now())
                .build();

        when(incidentTicketRepository.save(any(IncidentTicketEntity.class))).thenReturn(createdTicket);

        FileIncidentTicketRequest request = FileIncidentTicketRequest.builder()
                .targetEntityId(storeId)
                .targetEntityType(EntityType.STORE)
                .subject("Defective Product")
                .description("Product was broken upon delivery")
                .build();

        IncidentTicketResponse ticketResponse = trustService.fileIncidentTicket(reporterId, request);

        assertNotNull(ticketResponse);
        assertEquals(TicketStatus.OPEN, ticketResponse.getStatus());
        assertEquals("Defective Product", ticketResponse.getSubject());

        // Verify complaints updated to 2, score recalculated (100 - 2 * 1.50 = 97.00 -> GOLD badge)
        verify(trustScoreRepository).save(argThat(entity ->
                entity.getComplaintsCount() == 2 &&
                entity.getTrustScore().compareTo(new BigDecimal("97.00")) == 0 &&
                entity.getBadgeLevel() == BadgeLevel.GOLD
        ));
    }

    @Test
    @DisplayName("updateTicketStatus - Transitions OPEN ticket to UNDER_REVIEW")
    void updateTicketStatus_Success() {
        IncidentTicketEntity ticket = IncidentTicketEntity.builder()
                .id(ticketId)
                .reporterId(reporterId)
                .targetEntityId(storeId)
                .targetEntityType(EntityType.STORE)
                .subject("Issue")
                .description("Desc")
                .status(TicketStatus.OPEN)
                .build();

        when(incidentTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(incidentTicketRepository.save(any(IncidentTicketEntity.class))).thenAnswer(i -> i.getArgument(0));

        IncidentTicketResponse response = trustService.updateTicketStatus(ticketId, TicketStatus.UNDER_REVIEW, adminId);

        assertEquals(TicketStatus.UNDER_REVIEW, response.getStatus());
    }

    @Test
    @DisplayName("updateTicketStatus - Throws InvalidDisputeTransitionException if updating terminal ticket")
    void updateTicketStatus_TerminalTicket_ThrowsException() {
        IncidentTicketEntity ticket = IncidentTicketEntity.builder()
                .id(ticketId)
                .status(TicketStatus.RESOLVED)
                .build();

        when(incidentTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(InvalidDisputeTransitionException.class, () ->
                trustService.updateTicketStatus(ticketId, TicketStatus.UNDER_REVIEW, adminId));
    }

    @Test
    @DisplayName("resolveIncidentTicket - Approved resolution sets RESOLVED status")
    void resolveIncidentTicket_Approved_Success() {
        IncidentTicketEntity ticket = IncidentTicketEntity.builder()
                .id(ticketId)
                .reporterId(reporterId)
                .targetEntityId(storeId)
                .targetEntityType(EntityType.STORE)
                .subject("Issue")
                .description("Desc")
                .status(TicketStatus.UNDER_REVIEW)
                .build();

        when(incidentTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(incidentTicketRepository.save(any(IncidentTicketEntity.class))).thenAnswer(i -> i.getArgument(0));

        ResolveIncidentTicketRequest request = ResolveIncidentTicketRequest.builder()
                .resolutionNotes("Verified issue, refund issued")
                .approve(true)
                .build();

        IncidentTicketResponse response = trustService.resolveIncidentTicket(ticketId, request, adminId);

        assertEquals(TicketStatus.RESOLVED, response.getStatus());
        assertEquals("Verified issue, refund issued", response.getResolutionNotes());
        assertEquals(adminId, response.getResolvedBy());
    }

    @Test
    @DisplayName("resolveIncidentTicket - Rejected resolution reverses complaint count and restores score")
    void resolveIncidentTicket_Rejected_ReversesComplaintPenalty() {
        IncidentTicketEntity ticket = IncidentTicketEntity.builder()
                .id(ticketId)
                .reporterId(reporterId)
                .targetEntityId(storeId)
                .targetEntityType(EntityType.STORE)
                .subject("False complaint")
                .description("Desc")
                .status(TicketStatus.OPEN)
                .build();

        TrustScoreEntity trustEntity = TrustScoreEntity.builder()
                .id(UUID.randomUUID())
                .entityId(storeId)
                .entityType(EntityType.STORE)
                .complaintsCount(1)
                .trustScore(new BigDecimal("98.50"))
                .badgeLevel(BadgeLevel.PLATINUM)
                .build();

        when(incidentTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(incidentTicketRepository.save(any(IncidentTicketEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(trustScoreRepository.findByEntityIdAndEntityType(storeId, EntityType.STORE)).thenReturn(Optional.of(trustEntity));

        ResolveIncidentTicketRequest request = ResolveIncidentTicketRequest.builder()
                .resolutionNotes("Dispute groundless")
                .approve(false)
                .build();

        IncidentTicketResponse response = trustService.resolveIncidentTicket(ticketId, request, adminId);

        assertEquals(TicketStatus.REJECTED, response.getStatus());

        // Verify complaint decremented back to 0, score restored to 100.00
        verify(trustScoreRepository).save(argThat(entity ->
                entity.getComplaintsCount() == 0 &&
                entity.getTrustScore().compareTo(new BigDecimal("100.00")) == 0 &&
                entity.getBadgeLevel() == BadgeLevel.PLATINUM
        ));
    }
}
