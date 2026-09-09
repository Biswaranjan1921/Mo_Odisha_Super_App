package com.statesmartlife.transport.dto;

import com.statesmartlife.transport.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportRouteResponse {
    private UUID id;
    private String routeNumber;
    private VehicleType vehicleType;
    private String startPoint;
    private String endPoint;
    private BigDecimal farePrice;
    private int frequencyMinutes;
    private boolean isActive;
}
