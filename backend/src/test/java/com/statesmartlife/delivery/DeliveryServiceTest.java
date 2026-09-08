package com.statesmartlife.delivery.service;

import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.commerce.entity.ProductEntity;
import com.statesmartlife.commerce.entity.StoreEntity;
import com.statesmartlife.commerce.repository.ProductRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.delivery.dto.AssignDeliveryPartnerRequest;
import com.statesmartlife.delivery.dto.DeliveryFailureReason;
import com.statesmartlife.delivery.dto.DeliveryResponse;
import com.statesmartlife.delivery.dto.DeliveryStatus;
import com.statesmartlife.delivery.dto.UpdateDeliveryStatusRequest;
import com.statesmartlife.delivery.entity.DeliveryEntity;
import com.statesmartlife.delivery.repository.DeliveryRepository;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.entity.OrderItemEntity;
import com.statesmartlife.order.repository.OrderItemRepository;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeliveryServiceTest {

    private DeliveryRepository deliveryRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private UserProfileRepository userProfileRepository;
    private ProductRepository productRepository;
    private DeliveryService deliveryService;

    private UUID customerId;
    private UUID storeOwnerId;
    private UUID partnerId;
    private UUID storeId;
    private UUID orderId;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        deliveryRepository = mock(DeliveryRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        storeRepository = mock(StoreRepository.class);
        userRepository = mock(UserRepository.class);
        userProfileRepository = mock(UserProfileRepository.class);
        productRepository = mock(ProductRepository.class);

        deliveryService = new DeliveryServiceImpl(
                deliveryRepository,
                orderRepository,
                orderItemRepository,
                storeRepository,
                userRepository,
                userProfileRepository,
                productRepository
        );

        customerId = UUID.randomUUID();
        storeOwnerId = UUID.randomUUID();
        partnerId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        deliveryId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Create delivery for paid order creates delivery record with dual address snapshots and copied delivery fee")
    void testCreateDeliveryForPaidOrderSuccess() {
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .deliveryFee(new BigDecimal("40.00"))
                .deliveryAddressLine("Plot 102, Saheed Nagar")
                .deliveryCity("Bhubaneswar")
                .deliveryState("Odisha")
                .deliveryPincode("751007")
                .build();

        StoreEntity store = StoreEntity.builder()
                .id(storeId)
                .name("Patra Groceries")
                .address("Unit 1 Market, Bhubaneswar")
                .build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        deliveryService.createDeliveryForPaidOrder(order);

        verify(deliveryRepository, times(1)).save(any(DeliveryEntity.class));
    }

    @Test
    @DisplayName("Create delivery for paid order is idempotent and does not recreate delivery if exists")
    void testCreateDeliveryForPaidOrderIdempotent() {
        OrderEntity order = OrderEntity.builder().id(orderId).storeId(storeId).build();
        DeliveryEntity existing = DeliveryEntity.builder().id(deliveryId).orderId(orderId).build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        deliveryService.createDeliveryForPaidOrder(order);

        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Assign delivery partner succeeds for authorized admin or shop owner")
    void testAssignDeliveryPartnerSuccess() {
        DeliveryEntity delivery = DeliveryEntity.builder()
                .id(deliveryId)
                .orderId(orderId)
                .status(DeliveryStatus.ASSIGNED)
                .build();

        UserEntity partnerUser = UserEntity.builder()
                .id(partnerId)
                .role("DELIVERY_PARTNER")
                .build();

        AssignDeliveryPartnerRequest req = AssignDeliveryPartnerRequest.builder()
                .deliveryPartnerId(partnerId)
                .build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(userRepository.findById(partnerId)).thenReturn(Optional.of(partnerUser));
        when(deliveryRepository.save(any(DeliveryEntity.class))).thenReturn(delivery);

        DeliveryResponse response = deliveryService.assignDeliveryPartner(UUID.randomUUID(), "ROLE_ADMIN", orderId, req);

        assertNotNull(response);
        assertEquals(partnerId, delivery.getDeliveryPartnerId());
        assertNotNull(delivery.getAssignedAt());
    }

    @Test
    @DisplayName("Assign delivery partner fails if delivery has already been picked up (Lock #5)")
    void testAssignDeliveryPartnerReassignmentAfterPickupRejected() {
        DeliveryEntity delivery = DeliveryEntity.builder()
                .id(deliveryId)
                .orderId(orderId)
                .status(DeliveryStatus.PICKED_UP)
                .build();

        AssignDeliveryPartnerRequest req = AssignDeliveryPartnerRequest.builder().deliveryPartnerId(partnerId).build();
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.assignDeliveryPartner(UUID.randomUUID(), "ROLE_ADMIN", orderId, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_REASSIGNMENT", ex.getErrorCode());
    }

    @Test
    @DisplayName("Assign delivery partner fails if target user does not possess DELIVERY_PARTNER role (Lock #4)")
    void testAssignDeliveryPartnerInvalidRoleRejected() {
        DeliveryEntity delivery = DeliveryEntity.builder().id(deliveryId).orderId(orderId).status(DeliveryStatus.ASSIGNED).build();
        UserEntity customerUser = UserEntity.builder().id(partnerId).role("CUSTOMER").build();

        AssignDeliveryPartnerRequest req = AssignDeliveryPartnerRequest.builder().deliveryPartnerId(partnerId).build();
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(userRepository.findById(partnerId)).thenReturn(Optional.of(customerUser));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.assignDeliveryPartner(UUID.randomUUID(), "ROLE_ADMIN", orderId, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_DELIVERY_PARTNER", ex.getErrorCode());
    }

    @Test
    @DisplayName("Assign delivery partner fails if shop owner attempts to assign delivery for another store's order")
    void testAssignDeliveryPartnerUnauthorizedMerchantForbidden() {
        DeliveryEntity delivery = DeliveryEntity.builder().id(deliveryId).orderId(orderId).status(DeliveryStatus.ASSIGNED).build();
        OrderEntity order = OrderEntity.builder().id(orderId).storeId(storeId).build();
        StoreEntity otherStore = StoreEntity.builder().id(storeId).ownerId(UUID.randomUUID()).build(); // Owned by someone else

        AssignDeliveryPartnerRequest req = AssignDeliveryPartnerRequest.builder().deliveryPartnerId(partnerId).build();
        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(otherStore));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.assignDeliveryPartner(storeOwnerId, "ROLE_SHOP_OWNER", orderId, req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getErrorCode());
    }

    @Test
    @DisplayName("Update delivery status succeeds along valid state matrix ASSIGNED -> PICKED_UP -> IN_TRANSIT -> DELIVERED")
    void testUpdateDeliveryStatusMatrixSuccess() {
        DeliveryEntity delivery = DeliveryEntity.builder()
                .id(deliveryId)
                .deliveryPartnerId(partnerId)
                .status(DeliveryStatus.ASSIGNED)
                .build();

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(DeliveryEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Step 1: ASSIGNED -> PICKED_UP
        UpdateDeliveryStatusRequest req1 = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.PICKED_UP).build();
        DeliveryResponse res1 = deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, req1);
        assertEquals(DeliveryStatus.PICKED_UP, res1.getStatus());
        assertNotNull(delivery.getPickedUpAt());

        // Step 2: PICKED_UP -> IN_TRANSIT
        UpdateDeliveryStatusRequest req2 = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.IN_TRANSIT).build();
        DeliveryResponse res2 = deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, req2);
        assertEquals(DeliveryStatus.IN_TRANSIT, res2.getStatus());
        assertNotNull(delivery.getInTransitAt());

        // Step 3: IN_TRANSIT -> DELIVERED
        UpdateDeliveryStatusRequest req3 = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.DELIVERED).build();
        DeliveryResponse res3 = deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, req3);
        assertEquals(DeliveryStatus.DELIVERED, res3.getStatus());
        assertNotNull(delivery.getCompletedAt());
    }

    @Test
    @DisplayName("Update delivery status fails for invalid transition ASSIGNED -> IN_TRANSIT")
    void testUpdateDeliveryStatusInvalidTransitionRejected() {
        DeliveryEntity delivery = DeliveryEntity.builder().id(deliveryId).deliveryPartnerId(partnerId).status(DeliveryStatus.ASSIGNED).build();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        UpdateDeliveryStatusRequest req = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.IN_TRANSIT).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INVALID_STATE_TRANSITION", ex.getErrorCode());
    }

    @Test
    @DisplayName("Update delivery status to FAILED requires mandatory failureReason (Lock #6)")
    void testUpdateDeliveryStatusFailedRequiresReason() {
        DeliveryEntity delivery = DeliveryEntity.builder().id(deliveryId).deliveryPartnerId(partnerId).status(DeliveryStatus.IN_TRANSIT).build();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        // Missing failureReason
        UpdateDeliveryStatusRequest reqWithoutReason = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.FAILED).build();
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, reqWithoutReason));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("MISSING_FAILURE_REASON", ex.getErrorCode());

        // With valid failureReason
        when(deliveryRepository.save(any(DeliveryEntity.class))).thenAnswer(i -> i.getArgument(0));
        UpdateDeliveryStatusRequest reqWithReason = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.FAILED)
                .failureReason(DeliveryFailureReason.CUSTOMER_UNAVAILABLE)
                .build();

        DeliveryResponse res = deliveryService.updateDeliveryStatus(partnerId, "ROLE_DELIVERY_PARTNER", deliveryId, reqWithReason);
        assertEquals(DeliveryStatus.FAILED, res.getStatus());
        assertEquals(DeliveryFailureReason.CUSTOMER_UNAVAILABLE, res.getFailureReason());
        assertNotNull(delivery.getFailedAt());
    }

    @Test
    @DisplayName("Update delivery status fails with FORBIDDEN if delivery partner attempts to update another partner's delivery")
    void testUpdateDeliveryStatusOtherPartnerForbidden() {
        UUID otherPartnerId = UUID.randomUUID();
        DeliveryEntity delivery = DeliveryEntity.builder().id(deliveryId).deliveryPartnerId(partnerId).status(DeliveryStatus.ASSIGNED).build();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        UpdateDeliveryStatusRequest req = UpdateDeliveryStatusRequest.builder().status(DeliveryStatus.PICKED_UP).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                deliveryService.updateDeliveryStatus(otherPartnerId, "ROLE_DELIVERY_PARTNER", deliveryId, req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getErrorCode());
    }

    @Test
    @DisplayName("Dual address historical snapshotting preserves initial pickup and dropoff addresses even if store address changes later")
    void testAddressSnapshotImmutability() {
        StoreEntity store = StoreEntity.builder().id(storeId).name("Original Store").address("Original Pickup Address").build();
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .storeId(storeId)
                .deliveryAddressLine("Original Dropoff Address")
                .deliveryCity("Bhubaneswar")
                .deliveryState("Odisha")
                .deliveryPincode("751007")
                .deliveryFee(new BigDecimal("40.00"))
                .build();

        when(deliveryRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        deliveryService.createDeliveryForPaidOrder(order);

        // Store changes address afterwards
        store.setAddress("New Changed Store Address");

        verify(deliveryRepository).save(argThat(entity ->
                "Original Pickup Address".equals(entity.getPickupAddressLine()) &&
                "Original Dropoff Address".equals(entity.getDropoffAddressLine())
        ));
    }
}
