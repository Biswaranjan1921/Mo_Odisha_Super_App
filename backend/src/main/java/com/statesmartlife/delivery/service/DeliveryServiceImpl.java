package com.statesmartlife.delivery.service;

import com.statesmartlife.auth.entity.UserEntity;
import com.statesmartlife.auth.repository.UserRepository;
import com.statesmartlife.commerce.entity.StoreEntity;
import com.statesmartlife.commerce.repository.ProductRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.core.exceptions.ResourceNotFoundException;
import com.statesmartlife.delivery.dto.AssignDeliveryPartnerRequest;
import com.statesmartlife.delivery.dto.DeliveryResponse;
import com.statesmartlife.delivery.dto.DeliveryStatus;
import com.statesmartlife.delivery.dto.PickupAddressDto;
import com.statesmartlife.delivery.dto.UpdateDeliveryStatusRequest;
import com.statesmartlife.delivery.entity.DeliveryEntity;
import com.statesmartlife.delivery.repository.DeliveryRepository;
import com.statesmartlife.order.dto.DeliveryAddressDto;
import com.statesmartlife.order.dto.OrderItemResponse;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.entity.OrderItemEntity;
import com.statesmartlife.order.repository.OrderItemRepository;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.user.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProductRepository productRepository;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository,
                               OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository,
                               StoreRepository storeRepository,
                               UserRepository userRepository,
                               UserProfileRepository userProfileRepository,
                               ProductRepository productRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void createDeliveryForPaidOrder(OrderEntity order) {
        // Idempotency check: 1 order = 1 delivery
        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        StoreEntity store = storeRepository.findById(order.getStoreId()).orElse(null);

        String pickupLine = (store != null && store.getAddress() != null) ? store.getAddress() : "Local Merchant Store";
        String pickupCity = "Bhubaneswar";
        String pickupState = "Odisha";
        String pickupPincode = "751001";

        String dropoffLine = order.getDeliveryAddressLine() != null ? order.getDeliveryAddressLine() : "Customer Address";
        String dropoffCity = order.getDeliveryCity() != null ? order.getDeliveryCity() : "Bhubaneswar";
        String dropoffState = order.getDeliveryState() != null ? order.getDeliveryState() : "Odisha";
        String dropoffPincode = order.getDeliveryPincode() != null ? order.getDeliveryPincode() : "751007";

        BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : new BigDecimal("40.00");

        DeliveryEntity delivery = DeliveryEntity.builder()
                .orderId(order.getId())
                .deliveryPartnerId(null)
                .pickupAddress(pickupLine)
                .dropoffAddress(dropoffLine)
                .pickupAddressLine(pickupLine)
                .pickupCity(pickupCity)
                .pickupState(pickupState)
                .pickupPincode(pickupPincode)
                .dropoffAddressLine(dropoffLine)
                .dropoffCity(dropoffCity)
                .dropoffState(dropoffState)
                .dropoffPincode(dropoffPincode)
                .status(DeliveryStatus.ASSIGNED)
                .deliveryFee(deliveryFee)
                .estimatedArrival(Instant.now().plus(Duration.ofMinutes(30)))
                .build();

        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public DeliveryResponse assignDeliveryPartner(UUID requestingUserId, String requestingUserRole, UUID orderId, AssignDeliveryPartnerRequest request) {
        DeliveryEntity delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));

        // Lock #5: Reassignment allowed only before pickup (ASSIGNED status)
        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BusinessRuleException("INVALID_REASSIGNMENT", "Only ASSIGNED deliveries can change delivery partner", HttpStatus.BAD_REQUEST);
        }

        // Authorization Check: ADMIN or authorized SHOP_OWNER
        boolean isAdmin = requestingUserRole != null && requestingUserRole.toUpperCase().contains("ADMIN");
        if (!isAdmin) {
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
            StoreEntity store = storeRepository.findById(order.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", order.getStoreId()));
            if (!store.getOwnerId().equals(requestingUserId)) {
                throw new BusinessRuleException("FORBIDDEN", "Access denied: Shop owner can only assign deliveries for own store orders", HttpStatus.FORBIDDEN);
            }
        }

        // Lock #4: Verify target user exists and has DELIVERY_PARTNER role
        UserEntity partnerUser = userRepository.findById(request.getDeliveryPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getDeliveryPartnerId()));

        String role = partnerUser.getRole() != null ? partnerUser.getRole().toUpperCase() : "";
        if (!role.contains("DELIVERY_PARTNER") && !role.contains("DELIVERY")) {
            throw new BusinessRuleException("INVALID_DELIVERY_PARTNER", "Target user does not possess DELIVERY_PARTNER role", HttpStatus.BAD_REQUEST);
        }

        delivery.setDeliveryPartnerId(partnerUser.getId());
        delivery.setAssignedAt(Instant.now());

        DeliveryEntity saved = deliveryRepository.save(delivery);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(UUID partnerUserId, String requestingUserRole, UUID deliveryId, UpdateDeliveryStatusRequest request) {
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        boolean isAdmin = requestingUserRole != null && requestingUserRole.toUpperCase().contains("ADMIN");

        // Service-level ownership check
        if (!isAdmin) {
            if (delivery.getDeliveryPartnerId() == null || !delivery.getDeliveryPartnerId().equals(partnerUserId)) {
                throw new BusinessRuleException("FORBIDDEN", "Access denied: Delivery partner can only update assigned deliveries", HttpStatus.FORBIDDEN);
            }
        }

        // Lock #2: Explicit state machine transition check
        if (!delivery.getStatus().canTransitionTo(request.getStatus())) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION",
                    "Cannot transition delivery status from " + delivery.getStatus() + " to " + request.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        // Lock #6: Mandatory failure reason when transition to FAILED
        if (request.getStatus() == DeliveryStatus.FAILED && request.getFailureReason() == null) {
            throw new BusinessRuleException("MISSING_FAILURE_REASON", "Failure reason is mandatory when marking delivery as FAILED", HttpStatus.BAD_REQUEST);
        }

        delivery.setStatus(request.getStatus());
        Instant now = Instant.now();

        if (request.getStatus() == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(now);
        } else if (request.getStatus() == DeliveryStatus.IN_TRANSIT) {
            delivery.setInTransitAt(now);
        } else if (request.getStatus() == DeliveryStatus.DELIVERED) {
            delivery.setCompletedAt(now);
        } else if (request.getStatus() == DeliveryStatus.FAILED) {
            delivery.setFailedAt(now);
            delivery.setFailureReason(request.getFailureReason());
        }

        DeliveryEntity updated = deliveryRepository.save(delivery);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryResponse> getAssignedDeliveries(UUID partnerUserId, Pageable pageable) {
        Page<DeliveryEntity> deliveries = deliveryRepository.findByDeliveryPartnerIdOrderByCreatedAtDesc(partnerUserId, pageable);
        return deliveries.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryForOrder(UUID requestingUserId, String requestingUserRole, UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        DeliveryEntity delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));

        boolean isAdmin = requestingUserRole != null && requestingUserRole.toUpperCase().contains("ADMIN");
        boolean isCustomer = order.getCustomerId().equals(requestingUserId);
        boolean isAssignedPartner = delivery.getDeliveryPartnerId() != null && delivery.getDeliveryPartnerId().equals(requestingUserId);
        
        boolean isStoreOwner = false;
        Optional<StoreEntity> storeOpt = storeRepository.findById(order.getStoreId());
        if (storeOpt.isPresent() && storeOpt.get().getOwnerId().equals(requestingUserId)) {
            isStoreOwner = true;
        }

        if (!isAdmin && !isCustomer && !isAssignedPartner && !isStoreOwner) {
            throw new BusinessRuleException("FORBIDDEN", "Access denied: Unauthorized to track delivery for order " + orderId, HttpStatus.FORBIDDEN);
        }

        return mapToResponse(delivery);
    }

    private DeliveryResponse mapToResponse(DeliveryEntity delivery) {
        String partnerName = null;
        if (delivery.getDeliveryPartnerId() != null) {
            partnerName = userProfileRepository.findByUserId(delivery.getDeliveryPartnerId())
                    .map(p -> p.getFullName())
                    .orElse("Delivery Agent (" + delivery.getDeliveryPartnerId().toString().substring(0, 8) + ")");
        }

        PickupAddressDto pickupDto = PickupAddressDto.builder()
                .addressLine1(delivery.getPickupAddressLine() != null ? delivery.getPickupAddressLine() : delivery.getPickupAddress())
                .city(delivery.getPickupCity())
                .state(delivery.getPickupState())
                .pincode(delivery.getPickupPincode())
                .build();

        DeliveryAddressDto dropoffDto = DeliveryAddressDto.builder()
                .addressLine1(delivery.getDropoffAddressLine() != null ? delivery.getDropoffAddressLine() : delivery.getDropoffAddress())
                .city(delivery.getDropoffCity())
                .state(delivery.getDropoffState())
                .pincode(delivery.getDropoffPincode())
                .build();

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(delivery.getOrderId());
        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            String prodName = productRepository.findById(item.getProductId())
                    .map(p -> p.getName())
                    .orElse("Item");
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(prodName)
                    .quantity(item.getQuantity())
                    .unitPriceAtPurchase(item.getUnitPriceAtPurchase())
                    .lineTotal(item.getLineTotal() != null ? item.getLineTotal() : item.getSubtotal())
                    .build();
        }).collect(Collectors.toList());

        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPartnerId(delivery.getDeliveryPartnerId())
                .deliveryPartnerName(partnerName)
                .pickupAddress(pickupDto)
                .dropoffAddress(dropoffDto)
                .status(delivery.getStatus())
                .deliveryFee(delivery.getDeliveryFee())
                .estimatedArrival(delivery.getEstimatedArrival())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .inTransitAt(delivery.getInTransitAt())
                .completedAt(delivery.getCompletedAt())
                .failedAt(delivery.getFailedAt())
                .failureReason(delivery.getFailureReason())
                .items(itemResponses)
                .createdAt(delivery.getCreatedAt())
                .build();
    }
}
