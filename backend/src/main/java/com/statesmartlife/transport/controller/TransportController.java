package com.statesmartlife.transport.controller;

import com.statesmartlife.transport.dto.CreateRouteRequest;
import com.statesmartlife.transport.dto.TransportRouteResponse;
import com.statesmartlife.transport.enums.VehicleType;
import com.statesmartlife.transport.service.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @PostMapping("/routes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransportRouteResponse> createRoute(
            @Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportService.createRoute(request));
    }

    @GetMapping("/routes")
    public ResponseEntity<List<TransportRouteResponse>> getAllActiveRoutes(
            @RequestParam(required = false) VehicleType vehicleType) {
        return ResponseEntity.ok(transportService.getAllActiveRoutes(vehicleType));
    }

    @GetMapping("/routes/search")
    public ResponseEntity<List<TransportRouteResponse>> searchRoutes(
            @RequestParam(required = false) String startPoint,
            @RequestParam(required = false) String endPoint) {
        return ResponseEntity.ok(transportService.searchRoutes(startPoint, endPoint));
    }
}
