package com.smartshop.modules.catalog.repository;

import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByIsDefaultDescIdAsc(Long productId);

    List<ProductVariant> findByProductIdAndStatusOrderByIsDefaultDescIdAsc(Long productId, VariantStatus status);

    List<ProductVariant> findByProductIdInAndStatus(List<Long> productIds, VariantStatus status);

    @EntityGraph(attributePaths = {"product", "product.store"})
    Optional<ProductVariant> findWithProductById(Long id);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    long countByProductIdAndStatus(Long productId, VariantStatus status);
}
