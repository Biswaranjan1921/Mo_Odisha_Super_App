package com.statesmartlife.delivery.repository;

import com.statesmartlife.delivery.entity.DeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {

    Optional<DeliveryEntity> findByOrderId(UUID orderId);

    Page<DeliveryEntity> findByDeliveryPartnerIdOrderByCreatedAtDesc(UUID deliveryPartnerId, Pageable pageable);
}
