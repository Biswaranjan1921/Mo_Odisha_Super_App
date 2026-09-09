package com.statesmartlife.emergency.service;

import com.statesmartlife.emergency.dto.DispatchVehicleRequest;
import com.statesmartlife.emergency.dto.EmergencyResponse;
import com.statesmartlife.emergency.dto.TriggerSosRequest;
import com.statesmartlife.emergency.dto.UpdateEmergencyStatusRequest;
import com.statesmartlife.emergency.entity.EmergencyRequestEntity;
import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.enums.ResponseServiceType;
import com.statesmartlife.emergency.exception.ActiveEmergencyAlreadyExistsException;
import com.statesmartlife.emergency.exception.EmergencyRequestNotFoundException;
import com.statesmartlife.emergency.exception.InvalidEmergencyTransitionException;
import com.statesmartlife.emergency.exception.UnauthorizedEmergencyAccessException;
import com.statesmartlife.emergency.repository.EmergencyRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EmergencyServiceImpl implements EmergencyService {

    private final EmergencyRequestRepository emergencyRepository;

    public EmergencyServiceImpl(EmergencyRequestRepository emergencyRepository) {
        this.emergencyRepository = emergencyRepository;
    }

    @Override
    @Transactional
    public EmergencyResponse triggerSos(UUID requesterId, String userPhone, TriggerSosRequest request) {
        // Active SOS duplicate check
        if (requesterId != null && emergencyRepository.existsByRequesterIdAndStatusIn(
                requesterId,
                List.of(EmergencyStatus.REPORTED, EmergencyStatus.DISPATCHED, EmergencyStatus.EN_ROUTE, EmergencyStatus.ON_SCENE))) {
            throw new ActiveEmergencyAlreadyExistsException("Citizen already has an active emergency request in progress");
        }

        // Coordinate range validation
        validateCoordinates(request.getLatitude(), request.getLongitude());

        String phone = (userPhone != null && !userPhone.isBlank()) ? userPhone : (request.getRequesterPhone() != null ? request.getRequesterPhone() : "108");

        EmergencyRequestEntity entity = EmergencyRequestEntity.builder()
                .requesterId(requesterId)
                .requesterPhone(phone)
                .emergencyType(request.getEmergencyType())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .addressText(request.getAddressText())
                .status(EmergencyStatus.REPORTED)
                .responseService(ResponseServiceType.AMBULANCE)
                .reportedAt(Instant.now())
                .build();

        EmergencyRequestEntity saved = emergencyRepository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmergencyResponse> getMyEmergencyRequests(UUID requesterId, Pageable pageable) {
        return emergencyRepository.findByRequesterIdOrderByReportedAtDesc(requesterId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmergencyResponse> getActiveEmergencyRequests(Pageable pageable) {
        return emergencyRepository.findByStatusInOrderByReportedAtDesc(
                List.of(EmergencyStatus.REPORTED, EmergencyStatus.DISPATCHED, EmergencyStatus.EN_ROUTE, EmergencyStatus.ON_SCENE),
                pageable
        ).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public EmergencyResponse dispatchVehicle(UUID currentUserId, String userRole, UUID emergencyId, DispatchVehicleRequest request) {
        EmergencyRequestEntity entity = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EmergencyRequestNotFoundException("Emergency request not found with ID: " + emergencyId));

        boolean isAdmin = userRole != null && userRole.toUpperCase().contains("ADMIN");
        if (!isAdmin) {
            throw new UnauthorizedEmergencyAccessException("Only authorized ADMIN or Command Center officers can dispatch emergency vehicles");
        }

        if (entity.getStatus() != EmergencyStatus.REPORTED) {
            throw new InvalidEmergencyTransitionException("Cannot dispatch vehicle for emergency request in status " + entity.getStatus());
        }

        entity.setResponseService(request.getResponseService());
        entity.setAssignedResponderId(request.getAssignedResponderId());
        if (request.getDispatcherNotes() != null) {
            entity.setDispatcherNotes(request.getDispatcherNotes());
        }
        entity.setStatus(EmergencyStatus.DISPATCHED);
        entity.setDispatchedAt(Instant.now());

        EmergencyRequestEntity updated = emergencyRepository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public EmergencyResponse updateEmergencyStatus(UUID currentUserId, String userRole, UUID emergencyId, UpdateEmergencyStatusRequest request) {
        EmergencyRequestEntity entity = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EmergencyRequestNotFoundException("Emergency request not found with ID: " + emergencyId));

        boolean isAdmin = userRole != null && userRole.toUpperCase().contains("ADMIN");
        boolean isAssignedResponder = entity.getAssignedResponderId() != null && entity.getAssignedResponderId().equals(currentUserId);

        if (!isAdmin && !isAssignedResponder) {
            throw new UnauthorizedEmergencyAccessException("User is not authorized to update status of emergency request " + emergencyId);
        }

        // Validate state machine transition matrix
        if (!entity.getStatus().canTransitionTo(request.getStatus())) {
            throw new InvalidEmergencyTransitionException("Cannot transition emergency status from " + entity.getStatus() + " to " + request.getStatus());
        }

        entity.setStatus(request.getStatus());
        Instant now = Instant.now();

        if (request.getStatus() == EmergencyStatus.EN_ROUTE) {
            entity.setEnRouteAt(now);
        } else if (request.getStatus() == EmergencyStatus.ON_SCENE) {
            entity.setOnSceneAt(now);
        } else if (request.getStatus() == EmergencyStatus.RESOLVED) {
            entity.setResolvedAt(now);
        }

        if (request.getDispatcherNotes() != null) {
            entity.setDispatcherNotes(request.getDispatcherNotes());
        }

        EmergencyRequestEntity updated = emergencyRepository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public EmergencyResponse cancelEmergencyRequest(UUID currentUserId, String userRole, UUID emergencyId) {
        EmergencyRequestEntity entity = emergencyRepository.findById(emergencyId)
                .orElseThrow(() -> new EmergencyRequestNotFoundException("Emergency request not found with ID: " + emergencyId));

        boolean isAdmin = userRole != null && userRole.toUpperCase().contains("ADMIN");
        boolean isRequester = entity.getRequesterId() != null && entity.getRequesterId().equals(currentUserId);

        if (!isAdmin && !isRequester) {
            throw new UnauthorizedEmergencyAccessException("User is not authorized to cancel emergency request " + emergencyId);
        }

        // Citizen can cancel only before ON_SCENE
        if (isRequester && !isAdmin && entity.getStatus() == EmergencyStatus.ON_SCENE) {
            throw new InvalidEmergencyTransitionException("Citizen cannot cancel emergency request once responder has arrived ON_SCENE");
        }

        if (entity.getStatus() == EmergencyStatus.RESOLVED || entity.getStatus() == EmergencyStatus.CANCELLED) {
            throw new InvalidEmergencyTransitionException("Cannot cancel emergency request in terminal status: " + entity.getStatus());
        }

        entity.setStatus(EmergencyStatus.CANCELLED);
        entity.setCancelledAt(Instant.now());

        EmergencyRequestEntity updated = emergencyRepository.save(entity);
        return mapToResponse(updated);
    }

    private void validateCoordinates(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) {
            throw new InvalidEmergencyTransitionException("Valid GPS coordinates are required");
        }
        if (lat.compareTo(new BigDecimal("-90.0")) < 0 || lat.compareTo(new BigDecimal("90.0")) > 0) {
            throw new InvalidEmergencyTransitionException("Latitude out of range [-90, +90]: " + lat);
        }
        if (lng.compareTo(new BigDecimal("-180.0")) < 0 || lng.compareTo(new BigDecimal("180.0")) > 0) {
            throw new InvalidEmergencyTransitionException("Longitude out of range [-180, +180]: " + lng);
        }
    }

    private EmergencyResponse mapToResponse(EmergencyRequestEntity entity) {
        return EmergencyResponse.builder()
                .id(entity.getId())
                .requesterId(entity.getRequesterId())
                .requesterPhone(entity.getRequesterPhone())
                .emergencyType(entity.getEmergencyType())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .addressText(entity.getAddressText())
                .status(entity.getStatus())
                .responseService(entity.getResponseService())
                .assignedResponderId(entity.getAssignedResponderId())
                .dispatcherNotes(entity.getDispatcherNotes())
                .reportedAt(entity.getReportedAt())
                .dispatchedAt(entity.getDispatchedAt())
                .enRouteAt(entity.getEnRouteAt())
                .onSceneAt(entity.getOnSceneAt())
                .resolvedAt(entity.getResolvedAt())
                .cancelledAt(entity.getCancelledAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .build();
    }
}
