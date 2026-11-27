package com.smartshop.modules.catalog.repository;

import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByStoreIdAndSlug(Long storeId, String slug);

    @EntityGraph(attributePaths = {"store", "category"})
    Optional<Product> findWithDetailsByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"store", "category"})
    @Query("""
        SELECT p FROM Product p
        WHERE p.id = :id
          AND p.deletedAt IS NULL
          AND p.status = 'ACTIVE'
          AND p.store.status = 'ACTIVE'
          AND p.category.status = 'ACTIVE'
        """)
    Optional<Product> findActivePublicById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"store", "category"})
    Page<Product> findByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"store", "category"})
    @Query("""
        SELECT p FROM Product p
        WHERE p.deletedAt IS NULL
          AND (:status IS NULL OR p.status = :status)
          AND (:storeId IS NULL OR p.store.id = :storeId)
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
        ORDER BY p.createdAt DESC
        """)
    Page<Product> findAllForAdmin(
        @Param("status") ProductStatus status,
        @Param("storeId") Long storeId,
        @Param("categoryId") Long categoryId,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"store", "category"})
    java.util.List<Product> findWithDetailsByIdIn(java.util.List<Long> ids);

    @Query(
        value = """
            SELECT p.id FROM Product p
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v
                  WHERE v.product = p AND v.status = 'ACTIVE' AND v.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v
                  WHERE v.product = p AND v.status = 'ACTIVE' AND v.price <= :maxPrice
              ))
            ORDER BY p.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(p.id) FROM Product p
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v
                  WHERE v.product = p AND v.status = 'ACTIVE' AND v.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v
                  WHERE v.product = p AND v.status = 'ACTIVE' AND v.price <= :maxPrice
              ))
            """
    )
    Page<Long> searchPublicProductIdsNewest(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("storeId") Long storeId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT p.id FROM Product p
            JOIN ProductVariant v ON v.product = p AND v.status = 'ACTIVE'
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price <= :maxPrice
              ))
            GROUP BY p.id, p.createdAt
            ORDER BY MIN(v.price) ASC, p.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Product p
            JOIN ProductVariant v ON v.product = p AND v.status = 'ACTIVE'
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price <= :maxPrice
              ))
            """
    )
    Page<Long> searchPublicProductIdsPriceAsc(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("storeId") Long storeId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT p.id FROM Product p
            JOIN ProductVariant v ON v.product = p AND v.status = 'ACTIVE'
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price <= :maxPrice
              ))
            GROUP BY p.id, p.createdAt
            ORDER BY MIN(v.price) DESC, p.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT p.id) FROM Product p
            JOIN ProductVariant v ON v.product = p AND v.status = 'ACTIVE'
            WHERE p.deletedAt IS NULL
              AND p.status = 'ACTIVE'
              AND p.store.status = 'ACTIVE'
              AND p.category.status = 'ACTIVE'
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:storeId IS NULL OR p.store.id = :storeId)
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))))
              AND (:minPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price >= :minPrice
              ))
              AND (:maxPrice IS NULL OR EXISTS (
                  SELECT 1 FROM ProductVariant v2
                  WHERE v2.product = p AND v2.status = 'ACTIVE' AND v2.price <= :maxPrice
              ))
            """
    )
    Page<Long> searchPublicProductIdsPriceDesc(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("storeId") Long storeId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
}
