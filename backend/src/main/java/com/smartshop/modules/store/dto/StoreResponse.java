package com.smartshop.modules.store.dto;

import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponse {

    private Long id;
    private Long userId;
    private String storeName;
    private String slug;
    private String description;
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
    private StoreStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
            .id(store.getId())
            .userId(store.getUser().getId())
            .storeName(store.getStoreName())
            .slug(store.getSlug())
            .description(store.getDescription())
            .logoUrl(store.getLogoUrl())
            .contactEmail(store.getContactEmail())
            .contactPhone(store.getContactPhone())
            .status(store.getStatus())
            .createdAt(store.getCreatedAt())
            .updatedAt(store.getUpdatedAt())
            .build();
    }
}
