package com.smartshop.modules.order.repository;

import com.smartshop.modules.order.entity.SellerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {

    @EntityGraph(attributePaths = {"store"})
    List<SellerOrder> findWithStoreByOrderIdOrderByIdAsc(Long orderId);

    @EntityGraph(attributePaths = {"store"})
    List<SellerOrder> findWithStoreByOrderIdInOrderByIdAsc(List<Long> orderIds);

    boolean existsBySellerOrderCode(String sellerOrderCode);
}
