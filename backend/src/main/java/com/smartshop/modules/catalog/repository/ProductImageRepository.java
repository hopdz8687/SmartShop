package com.smartshop.modules.catalog.repository;

import com.smartshop.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByIsPrimaryDescDisplayOrderAscIdAsc(Long productId);

    List<ProductImage> findByProductIdInAndIsPrimaryTrue(List<Long> productIds);

    @EntityGraph(attributePaths = {"product", "product.store"})
    Optional<ProductImage> findWithProductById(Long id);
}
