package com.statesmartlife.transport.service;

import com.statesmartlife.transport.dto.CreateRouteRequest;
import com.statesmartlife.transport.dto.TransportRouteResponse;
import com.statesmartlife.transport.entity.TransportRouteEntity;
import com.statesmartlife.transport.enums.VehicleType;
import com.statesmartlife.transport.repository.TransportRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransportServiceTest {

    private TransportRouteRepository routeRepository;
    private TransportServiceImpl transportService;

    @BeforeEach
    void setUp() {
        routeRepository = mock(TransportRouteRepository.class);
        transportService = new TransportServiceImpl(routeRepository);
    }

    @Test
    @DisplayName("createRoute - Creates active public transit route")
    void createRoute_Success() {
        when(routeRepository.save(any(TransportRouteEntity.class))).thenAnswer(i -> {
            TransportRouteEntity entity = i.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        CreateRouteRequest request = CreateRouteRequest.builder()
                .routeNumber("100")
                .vehicleType(VehicleType.MO_BUS)
                .startPoint("Bhubaneswar Master Canteen")
                .endPoint("Cuttack Badambadi")
                .farePrice(new BigDecimal("30.00"))
                .frequencyMinutes(10)
                .build();

        TransportRouteResponse response = transportService.createRoute(request);

        assertNotNull(response);
        assertEquals("100", response.getRouteNumber());
        assertEquals(VehicleType.MO_BUS, response.getVehicleType());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("getAllActiveRoutes - Filters by vehicle type when specified")
    void getAllActiveRoutes_FiltersByVehicleType() {
        TransportRouteEntity route = TransportRouteEntity.builder()
                .id(UUID.randomUUID())
                .routeNumber("100")
                .vehicleType(VehicleType.MO_BUS)
                .startPoint("Master Canteen")
                .endPoint("Airport")
                .isActive(true)
                .build();

        when(routeRepository.findByVehicleTypeAndIsActiveTrue(VehicleType.MO_BUS)).thenReturn(List.of(route));

        List<TransportRouteResponse> response = transportService.getAllActiveRoutes(VehicleType.MO_BUS);

        assertEquals(1, response.size());
        assertEquals("100", response.get(0).getRouteNumber());
    }
}
