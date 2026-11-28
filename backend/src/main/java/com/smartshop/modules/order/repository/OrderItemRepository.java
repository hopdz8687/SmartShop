package com.smartshop.modules.order.repository;

import com.smartshop.modules.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findBySellerOrderIdOrderByIdAsc(Long sellerOrderId);

    List<OrderItem> findBySellerOrderIdInOrderByIdAsc(List<Long> sellerOrderIds);
}
