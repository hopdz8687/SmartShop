package com.smartshop.modules.cart.repository;

import com.smartshop.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {
        "variant", "variant.product", "variant.product.store", "variant.product.category"
    })
    List<CartItem> findWithDetailsByCartIdOrderByCreatedAtDesc(Long cartId);

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    @EntityGraph(attributePaths = {
        "cart", "cart.user", "variant", "variant.product", "variant.product.store", "variant.product.category"
    })
    Optional<CartItem> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
        "cart", "cart.user", "variant", "variant.product", "variant.product.store", "variant.product.category"
    })
    List<CartItem> findWithDetailsByIdIn(List<Long> ids);

    void deleteByIdIn(List<Long> ids);
}
