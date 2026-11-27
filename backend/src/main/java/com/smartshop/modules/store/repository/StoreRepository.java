package com.smartshop.modules.store.repository;

import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.entity.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsBySlug(String slug);

    Optional<Store> findByIdAndStatusAndDeletedAtIsNull(Long id, StoreStatus status);
}
