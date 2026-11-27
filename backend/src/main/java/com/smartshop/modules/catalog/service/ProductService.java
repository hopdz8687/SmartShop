package com.smartshop.modules.catalog.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.common.util.SlugUtils;
import com.smartshop.modules.catalog.dto.CreateProductRequest;
import com.smartshop.modules.catalog.dto.ProductDetailResponse;
import com.smartshop.modules.catalog.dto.ProductImageRequest;
import com.smartshop.modules.catalog.dto.ProductImageResponse;
import com.smartshop.modules.catalog.dto.ProductSummaryResponse;
import com.smartshop.modules.catalog.dto.ProductVariantRequest;
import com.smartshop.modules.catalog.dto.ProductVariantResponse;
import com.smartshop.modules.catalog.dto.UpdateProductRequest;
import com.smartshop.modules.catalog.entity.Category;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductImage;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import com.smartshop.modules.catalog.repository.CategoryRepository;
import com.smartshop.modules.catalog.repository.ProductImageRepository;
import com.smartshop.modules.catalog.repository.ProductRepository;
import com.smartshop.modules.catalog.repository.ProductVariantRepository;
import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.entity.StoreStatus;
import com.smartshop.modules.store.repository.StoreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductService {

    private static final int MAX_SLUG_LENGTH = 250;

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    public ProductService(
        ProductRepository productRepository,
        ProductVariantRepository variantRepository,
        ProductImageRepository imageRepository,
        CategoryRepository categoryRepository,
        StoreRepository storeRepository
    ) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
    }

    // ─── Public Queries ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getPublicProducts(Pageable pageable) {
        Page<Long> idPage = productRepository.searchPublicProductIdsNewest(
            null, null, null, null, null, pageable
        );
        return toSummaryPageFromIds(idPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchPublicProducts(
        String keyword,
        Long categoryId,
        Long storeId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String sort,
        Pageable pageable
    ) {
        String cleanKeyword = trimToNull(keyword);
        String cleanSort = sort != null ? sort.trim().toLowerCase() : "newest";
        Page<Long> idPage = switch (cleanSort) {
            case "price_asc" -> productRepository.searchPublicProductIdsPriceAsc(
                cleanKeyword, categoryId, storeId, minPrice, maxPrice, pageable
            );
            case "price_desc" -> productRepository.searchPublicProductIdsPriceDesc(
                cleanKeyword, categoryId, storeId, minPrice, maxPrice, pageable
            );
            default -> productRepository.searchPublicProductIdsNewest(
                cleanKeyword, categoryId, storeId, minPrice, maxPrice, pageable
            );
        };
        return toSummaryPageFromIds(idPage);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getPublicProductDetail(Long productId) {
        Product product = productRepository.findActivePublicById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));

        List<ProductVariantResponse> variants = variantRepository
            .findByProductIdAndStatusOrderByIsDefaultDescIdAsc(productId, VariantStatus.ACTIVE)
            .stream()
            .map(ProductVariantResponse::from)
            .toList();

        List<ProductImageResponse> images = imageRepository
            .findByProductIdOrderByIsPrimaryDescDisplayOrderAscIdAsc(productId)
            .stream()
            .map(ProductImageResponse::from)
            .toList();

        return ProductDetailResponse.of(product, variants, images);
    }

    // ─── Admin Queries & Actions ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getAdminProducts(
        ProductStatus status,
        Long storeId,
        Long categoryId,
        Pageable pageable
    ) {
        Page<Product> page = productRepository.findAllForAdmin(status, storeId, categoryId, pageable);
        return toSummaryPage(page);
    }

    @Transactional
    public ProductDetailResponse updateProductStatusByAdmin(Long productId, ProductStatus status) {
        Product product = productRepository.findWithDetailsByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
        product.setStatus(status);
        Product saved = productRepository.saveAndFlush(product);
        return buildDetailResponse(saved);
    }

    // ─── Seller Product Management ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getSellerProducts(Long sellerUserId, Pageable pageable) {
        Store store = resolveSellerStore(sellerUserId);
        Page<Product> page = productRepository.findByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            store.getId(), pageable
        );
        return toSummaryPage(page);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getSellerProductDetail(Long sellerUserId, Long productId) {
        Store store = resolveSellerStore(sellerUserId);
        Product product = findOwnedProduct(productId, store.getId());
        return buildDetailResponse(product);
    }

    @Transactional
    public ProductDetailResponse createProduct(Long sellerUserId, CreateProductRequest request) {
        Store store = resolveSellerStore(sellerUserId);
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "STORE_NOT_ACTIVE", "Store is not active");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
            .filter(c -> c.getStatus() == CategoryStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Active category not found"));

        List<ProductVariantRequest> variantReqs = request.getVariants();
        validateInitialVariants(variantReqs);

        String slug = allocateUniqueProductSlug(store.getId(), request.getName().trim());
        Product product = Product.builder()
            .store(store)
            .category(category)
            .name(request.getName().trim())
            .slug(slug)
            .description(trimToNull(request.getDescription()))
            .brand(trimToNull(request.getBrand()))
            .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
            .build();

        Product savedProduct = productRepository.saveAndFlush(product);

        boolean autoMarkDefault = variantReqs.stream().noneMatch(v -> Boolean.TRUE.equals(v.getIsDefault()));
        boolean defaultAssigned = false;

        for (ProductVariantRequest vReq : variantReqs) {
            VariantStatus vStatus = vReq.getStatus() != null ? vReq.getStatus() : VariantStatus.ACTIVE;
            boolean isDefault = Boolean.TRUE.equals(vReq.getIsDefault());
            if (autoMarkDefault && !defaultAssigned && vStatus == VariantStatus.ACTIVE) {
                isDefault = true;
                defaultAssigned = true;
            }

            String sku = vReq.getSku().trim();
            if (variantRepository.existsBySku(sku)) {
                throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
            }

            ProductVariant variant = ProductVariant.builder()
                .product(savedProduct)
                .sku(sku)
                .variantName(trimToNull(vReq.getVariantName()))
                .price(vReq.getPrice())
                .stockQuantity(vReq.getStockQuantity())
                .isDefault(isDefault)
                .status(vStatus)
                .build();
            try {
                variantRepository.saveAndFlush(variant);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
            }
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveInitialImages(savedProduct, request.getImages());
        }

        return buildDetailResponse(savedProduct);
    }

    @Transactional
    public ProductDetailResponse updateProduct(
        Long sellerUserId,
        Long productId,
        UpdateProductRequest request
    ) {
        Store store = resolveSellerStore(sellerUserId);
        Product product = findOwnedProduct(productId, store.getId());

        Category category = categoryRepository.findById(request.getCategoryId())
            .filter(c -> c.getStatus() == CategoryStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND", "Active category not found"));

        String newName = request.getName().trim();
        if (!newName.equalsIgnoreCase(product.getName())) {
            product.setSlug(allocateUniqueProductSlug(store.getId(), newName));
        }

        product.setCategory(category);
        product.setName(newName);
        product.setDescription(trimToNull(request.getDescription()));
        product.setBrand(trimToNull(request.getBrand()));
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product saved = productRepository.saveAndFlush(product);
        return buildDetailResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long sellerUserId, Long productId) {
        Store store = resolveSellerStore(sellerUserId);
        Product product = findOwnedProduct(productId, store.getId());
        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.saveAndFlush(product);
    }

    // ─── Seller Variant Management ──────────────────────────────────────────────

    @Transactional
    public ProductVariantResponse addVariant(
        Long sellerUserId,
        Long productId,
        ProductVariantRequest request
    ) {
        Store store = resolveSellerStore(sellerUserId);
        Product product = findOwnedProduct(productId, store.getId());

        String sku = request.getSku().trim();
        if (variantRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
        }

        VariantStatus status = request.getStatus() != null ? request.getStatus() : VariantStatus.ACTIVE;
        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (isDefault && status != VariantStatus.ACTIVE) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_DEFAULT_VARIANT",
                "Default variant must be ACTIVE"
            );
        }

        if (isDefault) {
            clearDefaultFlag(productId);
        }

        ProductVariant variant = ProductVariant.builder()
            .product(product)
            .sku(sku)
            .variantName(trimToNull(request.getVariantName()))
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity())
            .isDefault(isDefault)
            .status(status)
            .build();

        try {
            ProductVariant saved = variantRepository.saveAndFlush(variant);
            return ProductVariantResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
        }
    }

    @Transactional
    public ProductVariantResponse updateVariant(
        Long sellerUserId,
        Long variantId,
        ProductVariantRequest request
    ) {
        Store store = resolveSellerStore(sellerUserId);
        ProductVariant variant = variantRepository.findWithProductById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND", "Variant not found"));

        Product product = variant.getProduct();
        if (product.getDeletedAt() != null || !product.getStore().getId().equals(store.getId())) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to modify this variant"
            );
        }

        String sku = request.getSku().trim();
        if (variantRepository.existsBySkuAndIdNot(sku, variantId)) {
            throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
        }

        VariantStatus targetStatus = request.getStatus() != null ? request.getStatus() : variant.getStatus();
        boolean targetDefault = request.getIsDefault() != null ? request.getIsDefault() : Boolean.TRUE.equals(variant.getIsDefault());

        if (targetDefault && targetStatus != VariantStatus.ACTIVE) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_DEFAULT_VARIANT",
                "Default variant must be ACTIVE"
            );
        }
        if (Boolean.TRUE.equals(variant.getIsDefault()) && !targetDefault) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "DEFAULT_VARIANT_REQUIRED",
                "Cannot unset default variant directly; mark another active variant as default instead"
            );
        }
        if (variant.getStatus() == VariantStatus.ACTIVE && targetStatus == VariantStatus.INACTIVE) {
            long activeCount = variantRepository.countByProductIdAndStatus(product.getId(), VariantStatus.ACTIVE);
            if (activeCount <= 1) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "LAST_ACTIVE_VARIANT",
                    "Product must have at least one ACTIVE variant"
                );
            }
        }

        if (targetDefault && !Boolean.TRUE.equals(variant.getIsDefault())) {
            clearDefaultFlag(product.getId());
        }

        variant.setSku(sku);
        variant.setVariantName(trimToNull(request.getVariantName()));
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setIsDefault(targetDefault);
        variant.setStatus(targetStatus);

        try {
            ProductVariant saved = variantRepository.saveAndFlush(variant);
            return ProductVariantResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("SKU_ALREADY_EXISTS", "SKU already exists: " + sku);
        }
    }

    @Transactional
    public void deactivateVariant(Long sellerUserId, Long variantId) {
        Store store = resolveSellerStore(sellerUserId);
        ProductVariant variant = variantRepository.findWithProductById(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND", "Variant not found"));

        Product product = variant.getProduct();
        if (product.getDeletedAt() != null || !product.getStore().getId().equals(store.getId())) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to deactivate this variant"
            );
        }

        if (Boolean.TRUE.equals(variant.getIsDefault())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CANNOT_DEACTIVATE_DEFAULT_VARIANT",
                "Cannot deactivate the default variant; set another active variant as default first"
            );
        }

        if (variant.getStatus() == VariantStatus.ACTIVE) {
            long activeCount = variantRepository.countByProductIdAndStatus(product.getId(), VariantStatus.ACTIVE);
            if (activeCount <= 1) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "LAST_ACTIVE_VARIANT",
                    "Product must have at least one ACTIVE variant"
                );
            }
            variant.setStatus(VariantStatus.INACTIVE);
            variantRepository.saveAndFlush(variant);
        }
    }

    // ─── Seller Image Management ────────────────────────────────────────────────

    @Transactional
    public ProductImageResponse addImage(
        Long sellerUserId,
        Long productId,
        ProductImageRequest request
    ) {
        Store store = resolveSellerStore(sellerUserId);
        Product product = findOwnedProduct(productId, store.getId());

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId())
                .filter(v -> v.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException(
                    "VARIANT_NOT_FOUND", "Variant not found for this product"
                ));
        }

        boolean isPrimary = Boolean.TRUE.equals(request.getIsPrimary());
        if (isPrimary) {
            clearPrimaryImageFlag(productId);
        }

        ProductImage image = ProductImage.builder()
            .product(product)
            .variant(variant)
            .imageUrl(request.getImageUrl().trim())
            .altText(trimToNull(request.getAltText()))
            .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
            .isPrimary(isPrimary)
            .build();

        ProductImage saved = imageRepository.saveAndFlush(image);
        return ProductImageResponse.from(saved);
    }

    @Transactional
    public void deleteImage(Long sellerUserId, Long imageId) {
        Store store = resolveSellerStore(sellerUserId);
        ProductImage image = imageRepository.findWithProductById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("IMAGE_NOT_FOUND", "Product image not found"));

        Product product = image.getProduct();
        if (product.getDeletedAt() != null || !product.getStore().getId().equals(store.getId())) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to delete this image"
            );
        }

        imageRepository.delete(image);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Store resolveSellerStore(Long sellerUserId) {
        return storeRepository.findByUserId(sellerUserId)
            .filter(s -> s.getDeletedAt() == null)
            .orElseThrow(() -> new ResourceNotFoundException("STORE_NOT_FOUND", "Store not found for current seller"));
    }

    private Product findOwnedProduct(Long productId, Long storeId) {
        Product product = productRepository.findWithDetailsByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
        if (!product.getStore().getId().equals(storeId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this product"
            );
        }
        return product;
    }

    private void validateInitialVariants(List<ProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "VARIANT_REQUIRED",
                "Product must have at least one variant"
            );
        }

        Set<String> seenSkus = new HashSet<>();
        int activeCount = 0;
        int defaultCount = 0;

        for (ProductVariantRequest v : variants) {
            String sku = v.getSku().trim();
            if (!seenSkus.add(sku)) {
                throw new DuplicateResourceException("DUPLICATE_SKU", "Duplicate SKU in request: " + sku);
            }
            VariantStatus status = v.getStatus() != null ? v.getStatus() : VariantStatus.ACTIVE;
            boolean isDefault = Boolean.TRUE.equals(v.getIsDefault());
            if (status == VariantStatus.ACTIVE) {
                activeCount++;
            } else if (isDefault) {
                throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DEFAULT_VARIANT",
                    "Default variant must be ACTIVE"
                );
            }
            if (isDefault) {
                defaultCount++;
            }
        }

        if (activeCount == 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "ACTIVE_VARIANT_REQUIRED",
                "Product must have at least one ACTIVE variant"
            );
        }
        if (defaultCount > 1 || (defaultCount == 0 && variants.size() > 1)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_DEFAULT_VARIANT",
                "Product must have exactly one default active variant"
            );
        }
    }

    private void saveInitialImages(Product product, List<ProductImageRequest> imageRequests) {
        boolean hasPrimary = imageRequests.stream().anyMatch(i -> Boolean.TRUE.equals(i.getIsPrimary()));
        boolean primaryAssigned = false;

        for (int idx = 0; idx < imageRequests.size(); idx++) {
            ProductImageRequest req = imageRequests.get(idx);
            boolean isPrimary = Boolean.TRUE.equals(req.getIsPrimary());
            if (!hasPrimary && idx == 0) {
                isPrimary = true;
            }
            if (isPrimary && primaryAssigned) {
                isPrimary = false;
            }
            if (isPrimary) {
                primaryAssigned = true;
            }

            ProductImage img = ProductImage.builder()
                .product(product)
                .imageUrl(req.getImageUrl().trim())
                .altText(trimToNull(req.getAltText()))
                .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : idx)
                .isPrimary(isPrimary)
                .build();
            imageRepository.saveAndFlush(img);
        }
    }

    private void clearDefaultFlag(Long productId) {
        List<ProductVariant> existing = variantRepository.findByProductIdOrderByIsDefaultDescIdAsc(productId);
        for (ProductVariant v : existing) {
            if (Boolean.TRUE.equals(v.getIsDefault())) {
                v.setIsDefault(Boolean.FALSE);
                variantRepository.save(v);
            }
        }
        variantRepository.flush();
    }

    private void clearPrimaryImageFlag(Long productId) {
        List<ProductImage> existing = imageRepository.findByProductIdOrderByIsPrimaryDescDisplayOrderAscIdAsc(productId);
        for (ProductImage img : existing) {
            if (Boolean.TRUE.equals(img.getIsPrimary())) {
                img.setIsPrimary(Boolean.FALSE);
                imageRepository.save(img);
            }
        }
        imageRepository.flush();
    }

    private String allocateUniqueProductSlug(Long storeId, String name) {
        String base = SlugUtils.toSlugBase(name, MAX_SLUG_LENGTH, "product");
        if (!productRepository.existsByStoreIdAndSlug(storeId, base)) {
            return base;
        }
        for (int suffix = 2; suffix <= 50; suffix++) {
            String candidate = base + "-" + suffix;
            if (!productRepository.existsByStoreIdAndSlug(storeId, candidate)) {
                return candidate;
            }
        }
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private ProductDetailResponse buildDetailResponse(Product product) {
        List<ProductVariantResponse> variants = variantRepository
            .findByProductIdOrderByIsDefaultDescIdAsc(product.getId())
            .stream()
            .map(ProductVariantResponse::from)
            .toList();

        List<ProductImageResponse> images = imageRepository
            .findByProductIdOrderByIsPrimaryDescDisplayOrderAscIdAsc(product.getId())
            .stream()
            .map(ProductImageResponse::from)
            .toList();

        return ProductDetailResponse.of(product, variants, images);
    }

    private Page<ProductSummaryResponse> toSummaryPageFromIds(Page<Long> idPage) {
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return idPage.map(id -> null);
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : productRepository.findWithDetailsByIdIn(ids)) {
            productMap.put(p.getId(), p);
        }

        Map<Long, BigDecimal> minPrices = new HashMap<>();
        Map<Long, BigDecimal> maxPrices = new HashMap<>();
        for (ProductVariant v : variantRepository.findByProductIdInAndStatus(ids, VariantStatus.ACTIVE)) {
            Long pid = v.getProduct().getId();
            minPrices.merge(pid, v.getPrice(), BigDecimal::min);
            maxPrices.merge(pid, v.getPrice(), BigDecimal::max);
        }

        Map<Long, String> primaryImages = new HashMap<>();
        for (ProductImage img : imageRepository.findByProductIdInAndIsPrimaryTrue(ids)) {
            primaryImages.putIfAbsent(img.getProduct().getId(), img.getImageUrl());
        }

        return idPage.map(id -> {
            Product p = productMap.get(id);
            return ProductSummaryResponse.of(
                p,
                minPrices.get(id),
                maxPrices.get(id),
                primaryImages.get(id)
            );
        });
    }

    private Page<ProductSummaryResponse> toSummaryPage(Page<Product> page) {
        List<Long> productIds = page.getContent().stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            return page.map(p -> ProductSummaryResponse.of(p, null, null, null));
        }

        Map<Long, BigDecimal> minPrices = new HashMap<>();
        Map<Long, BigDecimal> maxPrices = new HashMap<>();
        for (ProductVariant v : variantRepository.findByProductIdInAndStatus(productIds, VariantStatus.ACTIVE)) {
            Long pid = v.getProduct().getId();
            minPrices.merge(pid, v.getPrice(), BigDecimal::min);
            maxPrices.merge(pid, v.getPrice(), BigDecimal::max);
        }

        Map<Long, String> primaryImages = new HashMap<>();
        for (ProductImage img : imageRepository.findByProductIdInAndIsPrimaryTrue(productIds)) {
            primaryImages.putIfAbsent(img.getProduct().getId(), img.getImageUrl());
        }

        return page.map(p -> ProductSummaryResponse.of(
            p,
            minPrices.get(p.getId()),
            maxPrices.get(p.getId()),
            primaryImages.get(p.getId())
        ));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
