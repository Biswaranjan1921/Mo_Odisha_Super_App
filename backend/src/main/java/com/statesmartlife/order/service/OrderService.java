package com.statesmartlife.order.service;

import com.statesmartlife.commerce.entity.ProductEntity;
import com.statesmartlife.commerce.entity.StoreEntity;
import com.statesmartlife.commerce.repository.InventoryRepository;
import com.statesmartlife.commerce.repository.ProductRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.core.exceptions.ResourceNotFoundException;
import com.statesmartlife.order.dto.CreateOrderRequest;
import com.statesmartlife.order.dto.DeliveryAddressDto;
import com.statesmartlife.order.dto.OrderItemRequest;
import com.statesmartlife.order.dto.OrderItemResponse;
import com.statesmartlife.order.dto.OrderResponse;
import com.statesmartlife.order.dto.OrderStatus;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.entity.OrderItemEntity;
import com.statesmartlife.order.repository.OrderItemRepository;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.payment.dto.PaymentResponse;
import com.statesmartlife.payment.dto.ProcessPaymentRequest;
import com.statesmartlife.payment.entity.PaymentEntity;
import com.statesmartlife.payment.provider.PaymentProvider;
import com.statesmartlife.payment.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final com.statesmartlife.delivery.service.DeliveryService deliveryService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        StoreRepository storeRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        PaymentRepository paymentRepository,
                        PaymentProvider paymentProvider,
                        com.statesmartlife.delivery.service.DeliveryService deliveryService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
        this.deliveryService = deliveryService;
    }

    @Transactional
    public OrderResponse createOrder(UUID customerId, CreateOrderRequest request) {
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", request.getStoreId()));

        List<ProductEntity> products = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // 1. Validation & Stock Reservation Phase
        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
                throw new BusinessRuleException("INVALID_QUANTITY", "Quantity must be at least 1", HttpStatus.BAD_REQUEST);
            }

            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (!product.getStoreId().equals(request.getStoreId())) {
                throw new BusinessRuleException("INVALID_STORE_ITEM",
                        "Product " + product.getName() + " does not belong to store " + request.getStoreId(),
                        HttpStatus.BAD_REQUEST);
            }

            // Prescription Guard
            if (product.isRequiresPrescription()) {
                throw new BusinessRuleException("PRESCRIPTION_REQUIRED_REJECTED",
                        "Order contains item '" + product.getName() + "' that requires a valid prescription",
                        HttpStatus.BAD_REQUEST);
            }

            // Atomic Inventory Deduction
            int updatedRows = inventoryRepository.deductStockAtomically(product.getId(), itemReq.getQuantity());
            if (updatedRows == 0) {
                throw new BusinessRuleException("INSUFFICIENT_STOCK",
                        "Product " + product.getName() + " has insufficient stock",
                        HttpStatus.BAD_REQUEST);
            }

            products.add(product);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal deliveryFee = new BigDecimal("40.00");
        BigDecimal totalAmount = subtotal.add(deliveryFee);

        // 2. Save Order Entity
        DeliveryAddressDto addr = request.getDeliveryAddress();
        OrderEntity order = OrderEntity.builder()
                .customerId(customerId)
                .storeId(store.getId())
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .totalAmount(totalAmount)
                .deliveryAddressLine(addr.getAddressLine1())
                .deliveryCity(addr.getCity())
                .deliveryState(addr.getState())
                .deliveryPincode(addr.getPincode())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        // 3. Save Order Item Entities with Price Snapshots
        List<OrderItemEntity> itemEntities = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemReq = request.getItems().get(i);
            ProductEntity product = products.get(i);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .quantity(itemReq.getQuantity())
                    .unitPriceAtPurchase(unitPrice)
                    .subtotal(lineTotal)
                    .lineTotal(lineTotal)
                    .build();

            itemEntities.add(orderItemRepository.save(itemEntity));
        }

        return mapToResponse(savedOrder, store.getName(), itemEntities, products, null);
    }

    @Transactional
    public OrderResponse processPayment(UUID customerId, UUID orderId, ProcessPaymentRequest request) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessRuleException("FORBIDDEN", "Access denied: Order does not belong to user", HttpStatus.FORBIDDEN);
        }

        // Single-Payment Idempotency Check
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessRuleException("ORDER_ALREADY_PAID", "Order " + orderId + " has already been paid for", HttpStatus.BAD_REQUEST);
        }

        // Delegate to Payment Provider
        PaymentResponse paymentResponse = paymentProvider.processPayment(order.getId(), request.getPaymentMethod(), order.getTotalAmount());

        // Update Order Status to PAID
        order.setStatus(OrderStatus.PAID);
        OrderEntity updatedOrder = orderRepository.save(order);

        // Trigger Part 10 Delivery Creation
        deliveryService.createDeliveryForPaidOrder(updatedOrder);

        // Retrieve Order Items & Products
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
        List<ProductEntity> products = items.stream()
                .map(item -> productRepository.findById(item.getProductId()).orElse(null))
                .collect(Collectors.toList());

        String storeName = storeRepository.findById(order.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Store");

        return mapToResponse(updatedOrder, storeName, items, products, paymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForCustomer(UUID customerId, Pageable pageable) {
        Page<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return orders.map(this::mapOrderToResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(UUID customerId, UUID orderId) {
        OrderEntity order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return mapOrderToResponse(order);
    }

    private OrderResponse mapOrderToResponse(OrderEntity order) {
        String storeName = storeRepository.findById(order.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Store");

        List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());
        List<ProductEntity> products = items.stream()
                .map(item -> productRepository.findById(item.getProductId()).orElse(null))
                .collect(Collectors.toList());

        Optional<PaymentEntity> paymentOpt = paymentRepository.findByOrderId(order.getId());
        PaymentResponse paymentResponse = paymentOpt.map(p -> PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .paymentProvider(p.getPaymentProvider())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .amount(p.getAmount())
                .paymentStatus(p.getPaymentStatus())
                .createdAt(p.getCreatedAt())
                .build()).orElse(null);

        return mapToResponse(order, storeName, items, products, paymentResponse);
    }

    private OrderResponse mapToResponse(OrderEntity order,
                                         String storeName,
                                         List<OrderItemEntity> items,
                                         List<ProductEntity> products,
                                         PaymentResponse payment) {
        DeliveryAddressDto addressDto = DeliveryAddressDto.builder()
                .addressLine1(order.getDeliveryAddressLine())
                .city(order.getDeliveryCity())
                .state(order.getDeliveryState())
                .pincode(order.getDeliveryPincode())
                .build();

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            OrderItemEntity item = items.get(i);
            ProductEntity product = (products != null && i < products.size()) ? products.get(i) : null;
            String productName = (product != null) ? product.getName() : "Product (" + item.getProductId() + ")";

            itemResponses.add(OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(productName)
                    .quantity(item.getQuantity())
                    .unitPriceAtPurchase(item.getUnitPriceAtPurchase())
                    .lineTotal(item.getLineTotal() != null ? item.getLineTotal() : item.getSubtotal())
                    .build());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .storeId(order.getStoreId())
                .storeName(storeName)
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(addressDto)
                .items(itemResponses)
                .payment(payment)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
