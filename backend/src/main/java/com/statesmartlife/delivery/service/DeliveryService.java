package com.statesmartlife.delivery.service;

import com.statesmartlife.delivery.dto.AssignDeliveryPartnerRequest;
import com.statesmartlife.delivery.dto.DeliveryResponse;
import com.statesmartlife.delivery.dto.UpdateDeliveryStatusRequest;
import com.statesmartlife.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryService {

    void createDeliveryForPaidOrder(OrderEntity order);

    DeliveryResponse assignDeliveryPartner(UUID requestingUserId, String requestingUserRole, UUID orderId, AssignDeliveryPartnerRequest request);

    DeliveryResponse updateDeliveryStatus(UUID partnerUserId, String requestingUserRole, UUID deliveryId, UpdateDeliveryStatusRequest request);

    Page<DeliveryResponse> getAssignedDeliveries(UUID partnerUserId, Pageable pageable);

    DeliveryResponse getDeliveryForOrder(UUID requestingUserId, String requestingUserRole, UUID orderId);
}
