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
public class PublicStoreResponse {

    private Long id;
    private String storeName;
    private String slug;
    private String description;
    private String logoUrl;
    private String contactEmail;
    private String contactPhone;
    private StoreStatus status;
    private LocalDateTime createdAt;

    public static PublicStoreResponse from(Store store) {
        return PublicStoreResponse.builder()
            .id(store.getId())
            .storeName(store.getStoreName())
            .slug(store.getSlug())
            .description(store.getDescription())
            .logoUrl(store.getLogoUrl())
            .contactEmail(store.getContactEmail())
            .contactPhone(store.getContactPhone())
            .status(store.getStatus())
            .createdAt(store.getCreatedAt())
            .build();
    }
}
