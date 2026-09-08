package com.statesmartlife.order.repository;

import com.statesmartlife.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Page<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Optional<OrderEntity> findByIdAndCustomerId(UUID id, UUID customerId);
}
