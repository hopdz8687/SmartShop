package com.smartshop.modules.store.service;

import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.store.dto.PublicStoreResponse;
import com.smartshop.modules.store.dto.StoreResponse;
import com.smartshop.modules.store.dto.UpdateStoreRequest;
import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.entity.StoreStatus;
import com.smartshop.modules.store.repository.StoreRepository;
import com.smartshop.modules.user.entity.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
public class StoreService {

    private static final int MAX_SLUG_BASE_LENGTH = 200;
    private static final int MAX_SLUG_RETRIES = 5;

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /**
     * Creates a single Store for an approved Seller inside an existing transaction.
     * Enforces one-store-per-seller and handles concurrent slug collisions safely.
     */
    @Transactional
    public Store createStoreForApprovedSeller(User owner, String businessName) {
        if (storeRepository.existsByUserId(owner.getId())) {
            throw new DuplicateResourceException("STORE_ALREADY_EXISTS", "User already owns a store");
        }

        String baseSlug = toSlugBase(businessName);
        String candidateSlug = findAvailableSlug(baseSlug);

        for (int attempt = 0; attempt < MAX_SLUG_RETRIES; attempt++) {
            Store store = Store.builder()
                .user(owner)
                .storeName(businessName.trim())
                .slug(candidateSlug)
                .contactEmail(owner.getEmail())
                .contactPhone(owner.getPhone())
                .status(StoreStatus.ACTIVE)
                .build();
            try {
                return storeRepository.saveAndFlush(store);
            } catch (DataIntegrityViolationException ex) {
                if (storeRepository.existsByUserId(owner.getId())) {
                    throw new DuplicateResourceException("STORE_ALREADY_EXISTS", "User already owns a store");
                }
                candidateSlug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
            }
        }

        throw new DuplicateResourceException("STORE_SLUG_CONFLICT", "Unable to allocate a unique store slug");
    }

    @Transactional(readOnly = true)
    public StoreResponse getMyStore(Long sellerUserId) {
        Store store = storeRepository.findByUserId(sellerUserId)
            .filter(s -> s.getDeletedAt() == null)
            .orElseThrow(() -> new ResourceNotFoundException("STORE_NOT_FOUND", "Store not found for current seller"));
        return StoreResponse.from(store);
    }

    @Transactional
    public StoreResponse updateMyStore(Long sellerUserId, UpdateStoreRequest request) {
        Store store = storeRepository.findByUserId(sellerUserId)
            .filter(s -> s.getDeletedAt() == null)
            .orElseThrow(() -> new ResourceNotFoundException("STORE_NOT_FOUND", "Store not found for current seller"));

        store.setStoreName(request.getStoreName().trim());
        store.setDescription(trimToNull(request.getDescription()));
        store.setLogoUrl(trimToNull(request.getLogoUrl()));
        store.setContactEmail(trimToNull(request.getContactEmail()));
        store.setContactPhone(trimToNull(request.getContactPhone()));

        Store saved = storeRepository.saveAndFlush(store);
        return StoreResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PublicStoreResponse getPublicStore(Long storeId) {
        Store store = storeRepository.findByIdAndStatusAndDeletedAtIsNull(storeId, StoreStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("STORE_NOT_FOUND", "Store not found"));
        return PublicStoreResponse.from(store);
    }

    private String findAvailableSlug(String baseSlug) {
        if (!storeRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }
        int suffix = 2;
        while (suffix <= 50) {
            String candidate = baseSlug + "-" + suffix;
            if (!storeRepository.existsBySlug(candidate)) {
                return candidate;
            }
            suffix++;
        }
        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String toSlugBase(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");

        if (normalized.isEmpty()) {
            return "store";
        }
        if (normalized.length() > MAX_SLUG_BASE_LENGTH) {
            return normalized.substring(0, MAX_SLUG_BASE_LENGTH).replaceAll("-+$", "");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
