package com.statesmartlife.transport.service;

import com.statesmartlife.transport.dto.CreateRouteRequest;
import com.statesmartlife.transport.dto.TransportRouteResponse;
import com.statesmartlife.transport.enums.VehicleType;

import java.util.List;

public interface TransportService {
    TransportRouteResponse createRoute(CreateRouteRequest request);
    List<TransportRouteResponse> getAllActiveRoutes(VehicleType vehicleType);
    List<TransportRouteResponse> searchRoutes(String startPoint, String endPoint);
}
