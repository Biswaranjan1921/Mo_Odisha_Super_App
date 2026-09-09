package com.statesmartlife.emergency.service;

import com.statesmartlife.emergency.dto.DispatchVehicleRequest;
import com.statesmartlife.emergency.dto.EmergencyResponse;
import com.statesmartlife.emergency.dto.TriggerSosRequest;
import com.statesmartlife.emergency.dto.UpdateEmergencyStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmergencyService {

    EmergencyResponse triggerSos(UUID requesterId, String userPhone, TriggerSosRequest request);

    Page<EmergencyResponse> getMyEmergencyRequests(UUID requesterId, Pageable pageable);

    Page<EmergencyResponse> getActiveEmergencyRequests(Pageable pageable);

    EmergencyResponse dispatchVehicle(UUID currentUserId, String userRole, UUID emergencyId, DispatchVehicleRequest request);

    EmergencyResponse updateEmergencyStatus(UUID currentUserId, String userRole, UUID emergencyId, UpdateEmergencyStatusRequest request);

    EmergencyResponse cancelEmergencyRequest(UUID currentUserId, String userRole, UUID emergencyId);
}
