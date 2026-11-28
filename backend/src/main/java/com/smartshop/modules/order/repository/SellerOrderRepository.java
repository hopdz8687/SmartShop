package com.smartshop.modules.order.repository;

import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.entity.SellerOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {

    @EntityGraph(attributePaths = {"store"})
    List<SellerOrder> findWithStoreByOrderIdOrderByIdAsc(Long orderId);

    @EntityGraph(attributePaths = {"store"})
    List<SellerOrder> findWithStoreByOrderIdInOrderByIdAsc(List<Long> orderIds);

    @EntityGraph(attributePaths = {"store", "order"})
    Page<SellerOrder> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"store", "order"})
    Optional<SellerOrder> findWithOrderAndStoreById(Long id);

    List<SellerOrder> findByOrderId(Long orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SellerOrder so
        SET so.status = :targetStatus, so.updatedAt = :now
        WHERE so.id = :id
          AND so.store.id = :storeId
          AND so.status = :expectedStatus
        """)
    int transitionStatusIfExpected(
        @Param("id") Long id,
        @Param("storeId") Long storeId,
        @Param("expectedStatus") SellerOrderStatus expectedStatus,
        @Param("targetStatus") SellerOrderStatus targetStatus,
        @Param("now") LocalDateTime now
    );

    boolean existsBySellerOrderCode(String sellerOrderCode);
}
