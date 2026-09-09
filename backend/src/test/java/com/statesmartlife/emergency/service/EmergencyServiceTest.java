package com.statesmartlife.emergency.service;

import com.statesmartlife.emergency.dto.DispatchVehicleRequest;
import com.statesmartlife.emergency.dto.EmergencyResponse;
import com.statesmartlife.emergency.dto.TriggerSosRequest;
import com.statesmartlife.emergency.dto.UpdateEmergencyStatusRequest;
import com.statesmartlife.emergency.entity.EmergencyRequestEntity;
import com.statesmartlife.emergency.enums.EmergencyStatus;
import com.statesmartlife.emergency.enums.EmergencyType;
import com.statesmartlife.emergency.enums.ResponseServiceType;
import com.statesmartlife.emergency.exception.ActiveEmergencyAlreadyExistsException;
import com.statesmartlife.emergency.exception.EmergencyRequestNotFoundException;
import com.statesmartlife.emergency.exception.InvalidEmergencyTransitionException;
import com.statesmartlife.emergency.exception.UnauthorizedEmergencyAccessException;
import com.statesmartlife.emergency.repository.EmergencyRequestRepository;
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

class EmergencyServiceTest {

    private EmergencyRequestRepository emergencyRepository;
    private EmergencyService emergencyService;

    private UUID requesterId;
    private UUID responderId;
    private UUID emergencyId;
    private EmergencyRequestEntity baseEntity;

    @BeforeEach
    void setUp() {
        emergencyRepository = mock(EmergencyRequestRepository.class);
        emergencyService = new EmergencyServiceImpl(emergencyRepository);

        requesterId = UUID.randomUUID();
        responderId = UUID.randomUUID();
        emergencyId = UUID.randomUUID();

        baseEntity = EmergencyRequestEntity.builder()
                .id(emergencyId)
                .requesterId(requesterId)
                .requesterPhone("+919988776655")
                .emergencyType(EmergencyType.MEDICAL)
                .latitude(new BigDecimal("20.2961000"))
                .longitude(new BigDecimal("85.8245000"))
                .addressText("Master Canteen Square, Bhubaneswar")
                .status(EmergencyStatus.REPORTED)
                .responseService(ResponseServiceType.AMBULANCE)
                .reportedAt(Instant.now())
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("triggerSos creates new emergency request in REPORTED status")
    void triggerSos_Success() {
        TriggerSosRequest request = TriggerSosRequest.builder()
                .emergencyType(EmergencyType.MEDICAL)
                .latitude(new BigDecimal("20.2961000"))
                .longitude(new BigDecimal("85.8245000"))
                .addressText("Master Canteen Square")
                .build();

        when(emergencyRepository.existsByRequesterIdAndStatusIn(eq(requesterId), any())).thenReturn(false);
        when(emergencyRepository.save(any(EmergencyRequestEntity.class))).thenReturn(baseEntity);

        EmergencyResponse response = emergencyService.triggerSos(requesterId, "+919988776655", request);

        assertNotNull(response);
        assertEquals(EmergencyStatus.REPORTED, response.getStatus());
        assertEquals(requesterId, response.getRequesterId());
        assertEquals(new BigDecimal("20.2961000"), response.getLatitude());
    }

    @Test
    @DisplayName("triggerSos throws ActiveEmergencyAlreadyExistsException (409) when active SOS exists")
    void triggerSos_ActiveDuplicate_Throws409() {
        TriggerSosRequest request = TriggerSosRequest.builder()
                .emergencyType(EmergencyType.FIRE)
                .latitude(new BigDecimal("20.2961000"))
                .longitude(new BigDecimal("85.8245000"))
                .build();

        when(emergencyRepository.existsByRequesterIdAndStatusIn(eq(requesterId), any())).thenReturn(true);

        assertThrows(ActiveEmergencyAlreadyExistsException.class, () -> emergencyService.triggerSos(requesterId, "+919988776655", request));
    }

    @Test
    @DisplayName("triggerSos throws InvalidEmergencyTransitionException (400) when coordinates are out of bounds")
    void triggerSos_InvalidCoordinates_Throws400() {
        TriggerSosRequest request = TriggerSosRequest.builder()
                .emergencyType(EmergencyType.ACCIDENT)
                .latitude(new BigDecimal("120.0000000")) // Invalid lat > 90
                .longitude(new BigDecimal("85.8245000"))
                .build();

        assertThrows(InvalidEmergencyTransitionException.class, () -> emergencyService.triggerSos(requesterId, "+919988776655", request));
    }

    @Test
    @DisplayName("dispatchVehicle by Admin assigns responder and transitions status to DISPATCHED")
    void dispatchVehicle_Admin_Success() {
        DispatchVehicleRequest request = DispatchVehicleRequest.builder()
                .responseService(ResponseServiceType.AMBULANCE)
                .assignedResponderId(responderId)
                .dispatcherNotes("Unit 108-Bhubaneswar Dispatched")
                .build();

        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));
        when(emergencyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmergencyResponse response = emergencyService.dispatchVehicle(UUID.randomUUID(), "ROLE_ADMIN", emergencyId, request);

        assertNotNull(response);
        assertEquals(EmergencyStatus.DISPATCHED, response.getStatus());
        assertEquals(responderId, response.getAssignedResponderId());
        assertNotNull(response.getDispatchedAt());
    }

    @Test
    @DisplayName("dispatchVehicle by Non-Admin throws UnauthorizedEmergencyAccessException (403)")
    void dispatchVehicle_NonAdmin_Throws403() {
        DispatchVehicleRequest request = DispatchVehicleRequest.builder()
                .responseService(ResponseServiceType.AMBULANCE)
                .build();

        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));

        assertThrows(UnauthorizedEmergencyAccessException.class,
                () -> emergencyService.dispatchVehicle(UUID.randomUUID(), "ROLE_CUSTOMER", emergencyId, request));
    }

    @Test
    @DisplayName("updateEmergencyStatus updates lifecycle timestamps and validates status matrix")
    void updateEmergencyStatus_Transitions() {
        baseEntity.setStatus(EmergencyStatus.DISPATCHED);
        baseEntity.setAssignedResponderId(responderId);

        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));
        when(emergencyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // DISPATCHED -> EN_ROUTE (Valid for assigned responder)
        UpdateEmergencyStatusRequest enRouteReq = new UpdateEmergencyStatusRequest(EmergencyStatus.EN_ROUTE, "En route to location");
        EmergencyResponse res1 = emergencyService.updateEmergencyStatus(responderId, "ROLE_DELIVERY_PARTNER", emergencyId, enRouteReq);
        assertEquals(EmergencyStatus.EN_ROUTE, res1.getStatus());
        assertNotNull(res1.getEnRouteAt());

        // EN_ROUTE -> RESOLVED (Invalid jump, missing ON_SCENE)
        UpdateEmergencyStatusRequest invalidReq = new UpdateEmergencyStatusRequest(EmergencyStatus.RESOLVED, "Done");
        assertThrows(InvalidEmergencyTransitionException.class,
                () -> emergencyService.updateEmergencyStatus(responderId, "ROLE_DELIVERY_PARTNER", emergencyId, invalidReq));
    }

    @Test
    @DisplayName("updateEmergencyStatus by unassigned responder throws UnauthorizedEmergencyAccessException (403)")
    void updateEmergencyStatus_UnassignedResponder_Throws403() {
        baseEntity.setStatus(EmergencyStatus.DISPATCHED);
        baseEntity.setAssignedResponderId(responderId);

        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));

        UUID otherUser = UUID.randomUUID();
        UpdateEmergencyStatusRequest req = new UpdateEmergencyStatusRequest(EmergencyStatus.EN_ROUTE, "Trying to update");

        assertThrows(UnauthorizedEmergencyAccessException.class,
                () -> emergencyService.updateEmergencyStatus(otherUser, "ROLE_CUSTOMER", emergencyId, req));
    }

    @Test
    @DisplayName("cancelEmergencyRequest by citizen before ON_SCENE marks request CANCELLED")
    void cancelEmergencyRequest_Requester_Success() {
        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));
        when(emergencyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmergencyResponse response = emergencyService.cancelEmergencyRequest(requesterId, "ROLE_CUSTOMER", emergencyId);

        assertNotNull(response);
        assertEquals(EmergencyStatus.CANCELLED, response.getStatus());
        assertNotNull(response.getCancelledAt());
    }

    @Test
    @DisplayName("cancelEmergencyRequest by citizen ON_SCENE throws InvalidEmergencyTransitionException (400)")
    void cancelEmergencyRequest_RequesterOnScene_Throws400() {
        baseEntity.setStatus(EmergencyStatus.ON_SCENE);
        when(emergencyRepository.findById(emergencyId)).thenReturn(Optional.of(baseEntity));

        assertThrows(InvalidEmergencyTransitionException.class,
                () -> emergencyService.cancelEmergencyRequest(requesterId, "ROLE_CUSTOMER", emergencyId));
    }
}
