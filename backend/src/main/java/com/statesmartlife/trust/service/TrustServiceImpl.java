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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrustServiceImpl implements TrustService {

    private final TrustScoreRepository trustScoreRepository;
    private final IncidentTicketRepository incidentTicketRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public TrustScoreResponse getTrustScore(UUID entityId, EntityType entityType) {
        validateEntityExists(entityId, entityType);
        TrustScoreEntity entity = trustScoreRepository.findByEntityIdAndEntityType(entityId, entityType)
                .orElseGet(() -> createInitialTrustScore(entityId, entityType));
        return mapToTrustScoreResponse(entity);
    }

    @Override
    @Transactional
    public TrustScoreResponse recordVerifiedTransaction(UUID entityId, EntityType entityType) {
        validateEntityExists(entityId, entityType);
        TrustScoreEntity entity = trustScoreRepository.findByEntityIdAndEntityType(entityId, entityType)
                .orElseGet(() -> createInitialTrustScore(entityId, entityType));

        entity.setVerifiedTransactionsCount(entity.getVerifiedTransactionsCount() + 1);
        recalculateScoreAndBadge(entity);
        TrustScoreEntity saved = trustScoreRepository.save(entity);
        return mapToTrustScoreResponse(saved);
    }

    @Override
    @Transactional
    public IncidentTicketResponse fileIncidentTicket(UUID reporterId, FileIncidentTicketRequest request) {
        validateEntityExists(request.getTargetEntityId(), request.getTargetEntityType());

        IncidentTicketEntity ticket = IncidentTicketEntity.builder()
                .reporterId(reporterId)
                .targetEntityId(request.getTargetEntityId())
                .targetEntityType(request.getTargetEntityType())
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .build();
        IncidentTicketEntity savedTicket = incidentTicketRepository.save(ticket);

        // Update target entity trust score complaints
        TrustScoreEntity trustEntity = trustScoreRepository.findByEntityIdAndEntityType(request.getTargetEntityId(), request.getTargetEntityType())
                .orElseGet(() -> createInitialTrustScore(request.getTargetEntityId(), request.getTargetEntityType()));

        trustEntity.setComplaintsCount(trustEntity.getComplaintsCount() + 1);
        recalculateScoreAndBadge(trustEntity);
        trustScoreRepository.save(trustEntity);

        return mapToIncidentTicketResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentTicketResponse> getMyIncidentTickets(UUID reporterId) {
        return incidentTicketRepository.findByReporterIdOrderByCreatedAtDesc(reporterId)
                .stream()
                .map(this::mapToIncidentTicketResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentTicketResponse> getAllIncidentTickets() {
        return incidentTicketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToIncidentTicketResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncidentTicketResponse updateTicketStatus(UUID ticketId, TicketStatus newStatus, UUID adminId) {
        IncidentTicketEntity ticket = incidentTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IncidentTicketNotFoundException("Incident ticket not found with id: " + ticketId));

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.REJECTED) {
            throw new InvalidDisputeTransitionException("Cannot change status of a terminal ticket (" + ticket.getStatus() + ")");
        }

        if (newStatus != TicketStatus.UNDER_REVIEW) {
            throw new InvalidDisputeTransitionException("Status can only be transitioned to UNDER_REVIEW via status update endpoint");
        }

        ticket.setStatus(TicketStatus.UNDER_REVIEW);
        IncidentTicketEntity saved = incidentTicketRepository.save(ticket);
        return mapToIncidentTicketResponse(saved);
    }

    @Override
    @Transactional
    public IncidentTicketResponse resolveIncidentTicket(UUID ticketId, ResolveIncidentTicketRequest request, UUID adminId) {
        IncidentTicketEntity ticket = incidentTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IncidentTicketNotFoundException("Incident ticket not found with id: " + ticketId));

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.REJECTED) {
            throw new InvalidDisputeTransitionException("Ticket has already been finalized with status: " + ticket.getStatus());
        }

        TicketStatus finalStatus = request.isApprove() ? TicketStatus.RESOLVED : TicketStatus.REJECTED;
        ticket.setStatus(finalStatus);
        ticket.setResolutionNotes(request.getResolutionNotes());
        ticket.setResolvedBy(adminId);
        ticket.setResolvedAt(Instant.now());

        if (!request.isApprove()) {
            // Ticket rejected (false complaint), reverse complaint penalty
            trustScoreRepository.findByEntityIdAndEntityType(ticket.getTargetEntityId(), ticket.getTargetEntityType())
                    .ifPresent(trustEntity -> {
                        int currentComplaints = trustEntity.getComplaintsCount();
                        if (currentComplaints > 0) {
                            trustEntity.setComplaintsCount(currentComplaints - 1);
                            recalculateScoreAndBadge(trustEntity);
                            trustScoreRepository.save(trustEntity);
                        }
                    });
        }

        IncidentTicketEntity saved = incidentTicketRepository.save(ticket);
        return mapToIncidentTicketResponse(saved);
    }

    private void validateEntityExists(UUID entityId, EntityType entityType) {
        boolean exists = switch (entityType) {
            case STORE -> storeRepository.existsById(entityId);
            case DELIVERY_PARTNER -> userRepository.existsById(entityId);
            case DOCTOR -> doctorRepository.existsById(entityId);
        };
        if (!exists) {
            throw new ResourceNotFoundException(entityType + " not found with id: " + entityId);
        }
    }

    private TrustScoreEntity createInitialTrustScore(UUID entityId, EntityType entityType) {
        TrustScoreEntity newEntity = TrustScoreEntity.builder()
                .entityId(entityId)
                .entityType(entityType)
                .verifiedTransactionsCount(0)
                .repeatUsersCount(0)
                .complaintsCount(0)
                .trustScore(new BigDecimal("100.00"))
                .badgeLevel(BadgeLevel.PLATINUM)
                .build();
        return trustScoreRepository.save(newEntity);
    }

    private void recalculateScoreAndBadge(TrustScoreEntity entity) {
        BigDecimal initial = new BigDecimal("100.00");
        BigDecimal complaintDeduction = BigDecimal.valueOf(entity.getComplaintsCount()).multiply(new BigDecimal("1.50"));
        BigDecimal transactionBonus = BigDecimal.valueOf(entity.getVerifiedTransactionsCount()).multiply(new BigDecimal("0.25"));

        BigDecimal score = initial.subtract(complaintDeduction).add(transactionBonus);
        if (score.compareTo(new BigDecimal("100.00")) > 0) {
            score = new BigDecimal("100.00");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        score = score.setScale(2, RoundingMode.HALF_UP);

        entity.setTrustScore(score);
        entity.setBadgeLevel(calculateBadgeLevel(score));
    }

    private BadgeLevel calculateBadgeLevel(BigDecimal score) {
        if (score.compareTo(new BigDecimal("98.00")) >= 0) {
            return BadgeLevel.PLATINUM;
        } else if (score.compareTo(new BigDecimal("90.00")) >= 0) {
            return BadgeLevel.GOLD;
        } else if (score.compareTo(new BigDecimal("80.00")) >= 0) {
            return BadgeLevel.SILVER;
        } else if (score.compareTo(new BigDecimal("70.00")) >= 0) {
            return BadgeLevel.BRONZE;
        } else {
            return BadgeLevel.NONE;
        }
    }

    private TrustScoreResponse mapToTrustScoreResponse(TrustScoreEntity entity) {
        return TrustScoreResponse.builder()
                .id(entity.getId())
                .entityId(entity.getEntityId())
                .entityType(entity.getEntityType())
                .verifiedTransactionsCount(entity.getVerifiedTransactionsCount())
                .repeatUsersCount(entity.getRepeatUsersCount())
                .complaintsCount(entity.getComplaintsCount())
                .trustScore(entity.getTrustScore())
                .badgeLevel(entity.getBadgeLevel())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private IncidentTicketResponse mapToIncidentTicketResponse(IncidentTicketEntity entity) {
        return IncidentTicketResponse.builder()
                .id(entity.getId())
                .reporterId(entity.getReporterId())
                .targetEntityId(entity.getTargetEntityId())
                .targetEntityType(entity.getTargetEntityType())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .resolutionNotes(entity.getResolutionNotes())
                .resolvedBy(entity.getResolvedBy())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
