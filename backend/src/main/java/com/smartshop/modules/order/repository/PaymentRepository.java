package com.smartshop.modules.order.repository;

import com.smartshop.modules.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySellerOrderId(Long sellerOrderId);
}
