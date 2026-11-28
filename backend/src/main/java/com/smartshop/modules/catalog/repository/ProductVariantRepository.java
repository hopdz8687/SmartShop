package com.smartshop.modules.catalog.repository;

import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderByIsDefaultDescIdAsc(Long productId);

    List<ProductVariant> findByProductIdAndStatusOrderByIsDefaultDescIdAsc(Long productId, VariantStatus status);

    List<ProductVariant> findByProductIdInAndStatus(List<Long> productIds, VariantStatus status);

    @EntityGraph(attributePaths = {"product", "product.store"})
    Optional<ProductVariant> findWithProductById(Long id);

    @EntityGraph(attributePaths = {"product", "product.store", "product.category"})
    Optional<ProductVariant> findWithFullProductById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ProductVariant v
        SET v.stockQuantity = v.stockQuantity - :quantity
        WHERE v.id = :variantId
          AND v.status = 'ACTIVE'
          AND v.stockQuantity >= :quantity
        """)
    int decrementStockIfAvailable(@Param("variantId") Long variantId, @Param("quantity") int quantity);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    long countByProductIdAndStatus(Long productId, VariantStatus status);
}
