package com.statesmartlife.commerce.service;

import com.statesmartlife.common.exception.BusinessRuleException;
import com.statesmartlife.commerce.dto.CommerceCategory;
import com.statesmartlife.commerce.dto.CreateProductRequest;
import com.statesmartlife.commerce.dto.CreateStoreRequest;
import com.statesmartlife.commerce.dto.ProductResponse;
import com.statesmartlife.commerce.dto.StoreResponse;
import com.statesmartlife.commerce.entity.InventoryEntity;
import com.statesmartlife.commerce.entity.ProductEntity;
import com.statesmartlife.commerce.entity.StoreEntity;
import com.statesmartlife.commerce.repository.InventoryRepository;
import com.statesmartlife.commerce.repository.ProductRepository;
import com.statesmartlife.commerce.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommerceServiceTest {

    private StoreRepository storeRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private CommerceService commerceService;

    private UUID ownerId;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        commerceService = new CommerceService(storeRepository, productRepository, inventoryRepository);

        ownerId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Create store succeeds for valid merchant request")
    void testCreateStoreSuccess() {
        CreateStoreRequest request = CreateStoreRequest.builder()
                .name("Patra Grocery Store")
                .category(CommerceCategory.GROCERY)
                .address("Bhubaneswar Market")
                .openingTime(LocalTime.of(8, 0))
                .closingTime(LocalTime.of(21, 0))
                .build();

        StoreEntity savedEntity = StoreEntity.builder()
                .id(storeId)
                .ownerId(ownerId)
                .name(request.getName())
                .category(request.getCategory())
                .address(request.getAddress())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .active(true)
                .build();

        when(storeRepository.save(any(StoreEntity.class))).thenReturn(savedEntity);

        StoreResponse response = commerceService.createStore(ownerId, request);

        assertNotNull(response);
        assertEquals("Patra Grocery Store", response.getName());
        assertEquals(CommerceCategory.GROCERY, response.getCategory());
        assertTrue(response.isActive());
    }

    @Test
    @DisplayName("Create product succeeds when ownerId matches store.ownerId")
    void testCreateProductSuccess() {
        StoreEntity store = StoreEntity.builder()
                .id(storeId)
                .ownerId(ownerId)
                .name("Valid Store")
                .build();

        CreateProductRequest request = CreateProductRequest.builder()
                .name("Basmati Rice 5kg")
                .price(new BigDecimal("350.00"))
                .initialStock(10)
                .isMedicine(false)
                .requiresPrescription(false)
                .build();

        UUID productId = UUID.randomUUID();
        ProductEntity savedProduct = ProductEntity.builder()
                .id(productId)
                .storeId(storeId)
                .name(request.getName())
                .price(request.getPrice())
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedProduct);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(InventoryEntity.builder().stockQuantity(10).build()));

        ProductResponse response = commerceService.createProduct(ownerId, storeId, request);

        assertNotNull(response);
        assertEquals("Basmati Rice 5kg", response.getName());
        assertEquals(10, response.getStockQuantity());
        assertTrue(response.isInStock());
    }

    @Test
    @DisplayName("Create product fails with 403 FORBIDDEN when user does not own store")
    void testCreateProductUnauthorizedStoreOwner() {
        UUID unauthorizedOwnerId = UUID.randomUUID();
        StoreEntity store = StoreEntity.builder()
                .id(storeId)
                .ownerId(ownerId) // Owned by ownerId
                .build();

        CreateProductRequest request = CreateProductRequest.builder()
                .name("Unauthorized Item")
                .price(new BigDecimal("100.00"))
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                commerceService.createProduct(unauthorizedOwnerId, storeId, request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("ACCESS_DENIED", ex.getErrorCode());
    }

    @Test
    @DisplayName("Atomic stock deduction succeeds when stock is available")
    void testDeductStockAtomicallySuccess() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(inventoryRepository.deductStockAtomically(eq(productId), eq(2))).thenReturn(1); // 1 row updated

        assertDoesNotThrow(() -> commerceService.deductStockAtomically(productId, 2));
    }

    @Test
    @DisplayName("Atomic stock deduction throws 400 BAD_REQUEST when stock is insufficient")
    void testDeductStockAtomicallyInsufficientStock() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(inventoryRepository.deductStockAtomically(eq(productId), eq(100))).thenReturn(0); // 0 rows updated

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                commerceService.deductStockAtomically(productId, 100));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("INSUFFICIENT_STOCK", ex.getErrorCode());
    }
}
