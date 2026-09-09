package com.statesmartlife.transport.service;

import com.statesmartlife.transport.dto.CreateRouteRequest;
import com.statesmartlife.transport.dto.TransportRouteResponse;
import com.statesmartlife.transport.entity.TransportRouteEntity;
import com.statesmartlife.transport.enums.VehicleType;
import com.statesmartlife.transport.repository.TransportRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {

    private final TransportRouteRepository routeRepository;

    @Override
    @Transactional
    public TransportRouteResponse createRoute(CreateRouteRequest request) {
        TransportRouteEntity route = TransportRouteEntity.builder()
                .routeNumber(request.getRouteNumber())
                .vehicleType(request.getVehicleType())
                .startPoint(request.getStartPoint())
                .endPoint(request.getEndPoint())
                .farePrice(request.getFarePrice())
                .frequencyMinutes(request.getFrequencyMinutes())
                .isActive(true)
                .build();

        TransportRouteEntity saved = routeRepository.save(route);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportRouteResponse> getAllActiveRoutes(VehicleType vehicleType) {
        List<TransportRouteEntity> routes;
        if (vehicleType != null) {
            routes = routeRepository.findByVehicleTypeAndIsActiveTrue(vehicleType);
        } else {
            routes = routeRepository.findByIsActiveTrue();
        }
        return routes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransportRouteResponse> searchRoutes(String startPoint, String endPoint) {
        return routeRepository.findByStartPointContainingIgnoreCaseAndEndPointContainingIgnoreCaseAndIsActiveTrue(
                        startPoint != null ? startPoint : "",
                        endPoint != null ? endPoint : ""
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransportRouteResponse mapToResponse(TransportRouteEntity entity) {
        return TransportRouteResponse.builder()
                .id(entity.getId())
                .routeNumber(entity.getRouteNumber())
                .vehicleType(entity.getVehicleType())
                .startPoint(entity.getStartPoint())
                .endPoint(entity.getEndPoint())
                .farePrice(entity.getFarePrice())
                .frequencyMinutes(entity.getFrequencyMinutes())
                .isActive(entity.isActive())
                .build();
    }
}
