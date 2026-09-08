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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommerceService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CommerceService(
            StoreRepository storeRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<StoreResponse> getStores(CommerceCategory category, Pageable pageable) {
        Page<StoreEntity> stores = category != null
                ? storeRepository.findByCategoryAndActiveTrue(category, pageable)
                : storeRepository.findByActiveTrue(pageable);

        return stores.map(this::toStoreResponse);
    }

    @Transactional(readOnly = true)
    public StoreResponse getStore(UUID storeId) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessRuleException("STORE_NOT_FOUND", "Store not found", HttpStatus.NOT_FOUND));
        return toStoreResponse(store);
    }

    @Transactional
    public StoreResponse createStore(UUID ownerId, CreateStoreRequest request) {
        StoreEntity store = StoreEntity.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .address(request.getAddress())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .active(true)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        StoreEntity saved = storeRepository.save(store);
        return toStoreResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStore(UUID storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new BusinessRuleException("STORE_NOT_FOUND", "Store not found", HttpStatus.NOT_FOUND);
        }
        return productRepository.findByStoreId(storeId, pageable).map(this::toProductResponse);
    }

    @Transactional
    public ProductResponse createProduct(UUID ownerId, UUID storeId, CreateProductRequest request) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessRuleException("STORE_NOT_FOUND", "Store not found", HttpStatus.NOT_FOUND));

        if (!store.getOwnerId().equals(ownerId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "You do not own this store", HttpStatus.FORBIDDEN);
        }

        ProductEntity product = ProductEntity.builder()
                .storeId(storeId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(request.getSku())
                .imageUrl(request.getImageUrl())
                .isMedicine(request.isMedicine())
                .requiresPrescription(request.isRequiresPrescription())
                .build();

        ProductEntity savedProduct = productRepository.save(product);

        InventoryEntity inventory = InventoryEntity.builder()
                .productId(savedProduct.getId())
                .stockQuantity(request.getInitialStock())
                .lowStockThreshold(5)
                .build();

        inventoryRepository.save(inventory);

        return toProductResponse(savedProduct);
    }

    @Transactional
    public void updateInventory(UUID ownerId, UUID productId, int stockQuantity) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessRuleException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));

        StoreEntity store = storeRepository.findById(product.getStoreId())
                .orElseThrow(() -> new BusinessRuleException("STORE_NOT_FOUND", "Store not found", HttpStatus.NOT_FOUND));

        if (!store.getOwnerId().equals(ownerId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "You do not own this store", HttpStatus.FORBIDDEN);
        }

        InventoryEntity inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> InventoryEntity.builder().productId(productId).build());

        inventory.setStockQuantity(stockQuantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deductStockAtomically(UUID productId, int quantity) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessRuleException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND);
        }

        int updatedRows = inventoryRepository.deductStockAtomically(productId, quantity);
        if (updatedRows == 0) {
            throw new BusinessRuleException("INSUFFICIENT_STOCK", "Insufficient stock available for this product", HttpStatus.BAD_REQUEST);
        }
    }

    private StoreResponse toStoreResponse(StoreEntity entity) {
        return StoreResponse.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .address(entity.getAddress())
                .openingTime(entity.getOpeningTime())
                .closingTime(entity.getClosingTime())
                .active(entity.isActive())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    private ProductResponse toProductResponse(ProductEntity entity) {
        int stock = inventoryRepository.findByProductId(entity.getId())
                .map(InventoryEntity::getStockQuantity)
                .orElse(0);

        return ProductResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .sku(entity.getSku())
                .imageUrl(entity.getImageUrl())
                .isMedicine(entity.isMedicine())
                .requiresPrescription(entity.isRequiresPrescription())
                .stockQuantity(stock)
                .inStock(stock > 0)
                .build();
    }
}
