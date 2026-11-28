package com.smartshop.modules.order.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.order.dto.OrderItemResponse;
import com.smartshop.modules.order.dto.OrderResponse;
import com.smartshop.modules.order.dto.PaymentResponse;
import com.smartshop.modules.order.dto.SellerOrderResponse;
import com.smartshop.modules.order.entity.Order;
import com.smartshop.modules.order.entity.OrderItem;
import com.smartshop.modules.order.entity.Payment;
import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.repository.OrderItemRepository;
import com.smartshop.modules.order.repository.OrderRepository;
import com.smartshop.modules.order.repository.PaymentRepository;
import com.smartshop.modules.order.repository.SellerOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(
        OrderRepository orderRepository,
        SellerOrderRepository sellerOrderRepository,
        OrderItemRepository orderItemRepository,
        PaymentRepository paymentRepository
    ) {
        this.orderRepository = orderRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Order> orders = ordersPage.getContent();
        if (orders.isEmpty()) {
            return ordersPage.map(o -> OrderResponse.of(o, List.of()));
        }

        Map<Long, List<SellerOrderResponse>> sellerOrdersByOrderId = loadSellerOrdersByOrderIds(
            orders.stream().map(Order::getId).toList()
        );

        return ordersPage.map(order -> OrderResponse.of(
            order,
            sellerOrdersByOrderId.getOrDefault(order.getId(), List.of())
        ));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to view this order"
            );
        }

        Map<Long, List<SellerOrderResponse>> sellerOrdersByOrderId = loadSellerOrdersByOrderIds(List.of(orderId));
        return OrderResponse.of(order, sellerOrdersByOrderId.getOrDefault(orderId, List.of()));
    }

    private Map<Long, List<SellerOrderResponse>> loadSellerOrdersByOrderIds(List<Long> orderIds) {
        List<SellerOrder> sellerOrders = sellerOrderRepository.findWithStoreByOrderIdInOrderByIdAsc(orderIds);
        if (sellerOrders.isEmpty()) {
            return Map.of();
        }

        List<Long> sellerOrderIds = sellerOrders.stream().map(SellerOrder::getId).toList();

        Map<Long, List<OrderItemResponse>> itemsBySellerOrderId = new HashMap<>();
        for (OrderItem item : orderItemRepository.findBySellerOrderIdInOrderByIdAsc(sellerOrderIds)) {
            itemsBySellerOrderId
                .computeIfAbsent(item.getSellerOrder().getId(), k -> new ArrayList<>())
                .add(OrderItemResponse.from(item));
        }

        Map<Long, PaymentResponse> paymentBySellerOrderId = new HashMap<>();
        for (Payment payment : paymentRepository.findBySellerOrderIdIn(sellerOrderIds)) {
            paymentBySellerOrderId.put(payment.getSellerOrder().getId(), PaymentResponse.from(payment));
        }

        Map<Long, List<SellerOrderResponse>> result = new HashMap<>();
        for (SellerOrder so : sellerOrders) {
            SellerOrderResponse response = SellerOrderResponse.of(
                so,
                itemsBySellerOrderId.getOrDefault(so.getId(), List.of()),
                paymentBySellerOrderId.get(so.getId())
            );
            result.computeIfAbsent(so.getOrder().getId(), k -> new ArrayList<>()).add(response);
        }
        return result;
    }
}
