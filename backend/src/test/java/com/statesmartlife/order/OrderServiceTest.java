package com.statesmartlife.order.service;

import com.statesmartlife.commerce.entity.ProductEntity;
import com.statesmartlife.commerce.entity.StoreEntity;
import com.statesmartlife.commerce.repository.InventoryRepository;
import com.statesmartlife.commerce.repository.ProductRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.order.dto.CreateOrderRequest;
import com.statesmartlife.order.dto.DeliveryAddressDto;
import com.statesmartlife.order.dto.OrderItemRequest;
import com.statesmartlife.order.dto.OrderResponse;
import com.statesmartlife.order.dto.OrderStatus;
import com.statesmartlife.order.entity.OrderEntity;
import com.statesmartlife.order.entity.OrderItemEntity;
import com.statesmartlife.order.repository.OrderItemRepository;
import com.statesmartlife.order.repository.OrderRepository;
import com.statesmartlife.payment.dto.PaymentResponse;
import com.statesmartlife.payment.dto.PaymentStatus;
import com.statesmartlife.payment.dto.ProcessPaymentRequest;
import com.statesmartlife.payment.provider.PaymentProvider;
import com.statesmartlife.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private StoreRepository storeRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private PaymentRepository paymentRepository;
    private PaymentProvider paymentProvider;
    private com.statesmartlife.delivery.service.DeliveryService deliveryService;
    private OrderService orderService;

    private UUID customerId;
    private UUID storeId;
    private UUID productId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        storeRepository = mock(StoreRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        paymentProvider = mock(PaymentProvider.class);
        deliveryService = mock(com.statesmartlife.delivery.service.DeliveryService.class);

        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                storeRepository,
                productRepository,
                inventoryRepository,
                paymentRepository,
                paymentProvider,
                deliveryService
        );

        customerId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Create order succeeds with backend-authoritative pricing and atomic stock deduction")
    void testCreateOrderSuccess() {
        StoreEntity store = StoreEntity.builder().id(storeId).name("Jan Aushadhi Pharmacy").build();
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .storeId(storeId)
                .name("Paracetamol 500mg")
                .price(new BigDecimal("25.00"))
                .isMedicine(true)
                .requiresPrescription(false)
                .build();

        DeliveryAddressDto address = DeliveryAddressDto.builder()
                .addressLine1("Plot 102, Saheed Nagar")
                .city("Bhubaneswar")
                .state("Odisha")
                .pincode("751007")
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(storeId)
                .items(List.of(OrderItemRequest.builder().productId(productId).quantity(2).build()))
                .deliveryAddress(address)
                .build();

        OrderEntity savedOrder = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(new BigDecimal("50.00"))
                .deliveryFee(new BigDecimal("40.00"))
                .totalAmount(new BigDecimal("90.00"))
                .deliveryAddressLine(address.getAddressLine1())
                .deliveryCity(address.getCity())
                .deliveryState(address.getState())
                .deliveryPincode(address.getPincode())
                .build();

        OrderItemEntity savedItem = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .productId(productId)
                .quantity(2)
                .unitPriceAtPurchase(new BigDecimal("25.00"))
                .subtotal(new BigDecimal("50.00"))
                .lineTotal(new BigDecimal("50.00"))
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.deductStockAtomically(eq(productId), eq(2))).thenReturn(1);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenReturn(savedItem);

        OrderResponse response = orderService.createOrder(customerId, request);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals(OrderStatus.PENDING_PAYMENT, response.getStatus());
        assertEquals(new BigDecimal("50.00"), response.getSubtotal());
        assertEquals(new BigDecimal("40.00"), response.getDeliveryFee());
        assertEquals(new BigDecimal("90.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("25.00"), response.getItems().get(0).getUnitPriceAtPurchase());
        verify(inventoryRepository, times(1)).deductStockAtomically(productId, 2);
    }

    @Test
    @DisplayName("Create order snapshots unit price at purchase time and remains immune to future price changes")
    void testCreateOrderPriceSnapshotImmutability() {
        StoreEntity store = StoreEntity.builder().id(storeId).name("Store").build();
        BigDecimal initialPrice = new BigDecimal("100.00");
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .storeId(storeId)
                .name("Basmati Rice 5kg")
                .price(initialPrice)
                .build();

        DeliveryAddressDto address = DeliveryAddressDto.builder()
                .addressLine1("Line 1")
                .city("City")
                .state("State")
                .pincode("751001")
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(storeId)
                .items(List.of(OrderItemRequest.builder().productId(productId).quantity(1).build()))
                .deliveryAddress(address)
                .build();

        OrderEntity savedOrder = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(initialPrice)
                .deliveryFee(new BigDecimal("40.00"))
                .totalAmount(new BigDecimal("140.00"))
                .build();

        OrderItemEntity savedItem = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .productId(productId)
                .quantity(1)
                .unitPriceAtPurchase(initialPrice)
                .subtotal(initialPrice)
                .lineTotal(initialPrice)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.deductStockAtomically(eq(productId), eq(1))).thenReturn(1);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItemEntity.class))).thenReturn(savedItem);

        OrderResponse response = orderService.createOrder(customerId, request);

        // Price in DB changes after order placement
        product.setPrice(new BigDecimal("150.00"));

        assertEquals(new BigDecimal("100.00"), response.getItems().get(0).getUnitPriceAtPurchase());
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
    }

    @Test
    @DisplayName("Create order fails when prescription required item is present")
    void testCreateOrderPrescriptionRequiredRejected() {
        StoreEntity store = StoreEntity.builder().id(storeId).name("Apollo Pharmacy").build();
        ProductEntity RxProduct = ProductEntity.builder()
                .id(productId)
                .storeId(storeId)
                .name("Antibiotic 500mg")
                .price(new BigDecimal("150.00"))
                .isMedicine(true)
                .requiresPrescription(true)
                .build();

        DeliveryAddressDto address = DeliveryAddressDto.builder()
                .addressLine1("Plot 102, Saheed Nagar")
                .city("Bhubaneswar")
                .state("Odisha")
                .pincode("751007")
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(storeId)
                .items(List.of(OrderItemRequest.builder().productId(productId).quantity(1).build()))
                .deliveryAddress(address)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(productRepository.findById(productId)).thenReturn(Optional.of(RxProduct));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                orderService.createOrder(customerId, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("PRESCRIPTION_REQUIRED_REJECTED", ex.getErrorCode());
        verify(inventoryRepository, never()).deductStockAtomically(any(), anyInt());
    }

    @Test
    @DisplayName("Create order fails when stock is insufficient")
    void testCreateOrderInsufficientStockRejected() {
        StoreEntity store = StoreEntity.builder().id(storeId).name("Store").build();
        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .storeId(storeId)
                .name("Item")
                .price(new BigDecimal("10.00"))
                .build();

        DeliveryAddressDto address = DeliveryAddressDto.builder()
                .addressLine1("Line 1")
                .city("City")
                .state("State")
                .pincode("751001")
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(storeId)
                .items(List.of(OrderItemRequest.builder().productId(productId).quantity(5).build()))
                .deliveryAddress(address)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryRepository.deductStockAtomically(eq(productId), eq(5))).thenReturn(0);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                orderService.createOrder(customerId, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INSUFFICIENT_STOCK", ex.getErrorCode());
    }

    @Test
    @DisplayName("Process payment succeeds and transitions order status to PAID")
    void testProcessPaymentSuccess() {
        OrderEntity pendingOrder = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(new BigDecimal("50.00"))
                .deliveryFee(new BigDecimal("40.00"))
                .totalAmount(new BigDecimal("90.00"))
                .deliveryAddressLine("Plot 102")
                .deliveryCity("Bhubaneswar")
                .deliveryState("Odisha")
                .deliveryPincode("751007")
                .build();

        OrderEntity paidOrder = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .storeId(storeId)
                .status(OrderStatus.PAID)
                .subtotal(new BigDecimal("50.00"))
                .deliveryFee(new BigDecimal("40.00"))
                .totalAmount(new BigDecimal("90.00"))
                .deliveryAddressLine("Plot 102")
                .deliveryCity("Bhubaneswar")
                .deliveryState("Odisha")
                .deliveryPincode("751007")
                .build();

        ProcessPaymentRequest payReq = ProcessPaymentRequest.builder()
                .paymentMethod("UPI")
                .build();

        PaymentResponse mockPaymentResponse = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .paymentProvider("MOCK_PROVIDER")
                .paymentMethod("UPI")
                .transactionId("MOCK-UPI-12345678")
                .amount(new BigDecimal("90.00"))
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));
        when(paymentProvider.processPayment(eq(orderId), eq("UPI"), eq(new BigDecimal("90.00"))))
                .thenReturn(mockPaymentResponse);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(paidOrder);
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(StoreEntity.builder().id(storeId).name("Store").build()));

        OrderResponse response = orderService.processPayment(customerId, orderId, payReq);

        assertNotNull(response);
        assertEquals(OrderStatus.PAID, response.getStatus());
        assertNotNull(response.getPayment());
        assertEquals("MOCK-UPI-12345678", response.getPayment().getTransactionId());
        verify(orderRepository, times(1)).save(pendingOrder);
    }

    @Test
    @DisplayName("Process payment fails with single-payment idempotency check if order is already PAID")
    void testProcessPaymentAlreadyPaidRejected() {
        OrderEntity paidOrder = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("90.00"))
                .build();

        ProcessPaymentRequest payReq = ProcessPaymentRequest.builder()
                .paymentMethod("UPI")
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(paidOrder));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                orderService.processPayment(customerId, orderId, payReq));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("ORDER_ALREADY_PAID", ex.getErrorCode());
        verify(paymentProvider, never()).processPayment(any(), any(), any());
    }

    @Test
    @DisplayName("Process payment fails with FORBIDDEN if order does not belong to user")
    void testProcessPaymentOtherUserForbidden() {
        UUID otherUserId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        ProcessPaymentRequest payReq = ProcessPaymentRequest.builder().paymentMethod("UPI").build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                orderService.processPayment(otherUserId, orderId, payReq));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getErrorCode());
    }
}
