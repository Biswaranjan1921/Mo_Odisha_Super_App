package com.statesmartlife.transport.repository;

import com.statesmartlife.transport.entity.TransportRouteEntity;
import com.statesmartlife.transport.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransportRouteRepository extends JpaRepository<TransportRouteEntity, UUID> {
    List<TransportRouteEntity> findByIsActiveTrue();
    List<TransportRouteEntity> findByVehicleTypeAndIsActiveTrue(VehicleType vehicleType);
    List<TransportRouteEntity> findByStartPointContainingIgnoreCaseAndEndPointContainingIgnoreCaseAndIsActiveTrue(String startPoint, String endPoint);
}
