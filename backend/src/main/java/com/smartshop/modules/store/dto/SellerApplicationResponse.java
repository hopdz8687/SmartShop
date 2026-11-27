package com.smartshop.modules.store.dto;

import com.smartshop.modules.store.entity.SellerApplication;
import com.smartshop.modules.store.entity.SellerApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationResponse {

    private Long id;
    private Long userId;
    private String applicantEmail;
    private String applicantName;
    private String businessName;
    private String applicantNote;
    private SellerApplicationStatus status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SellerApplicationResponse from(SellerApplication application) {
        return SellerApplicationResponse.builder()
            .id(application.getId())
            .userId(application.getUser().getId())
            .applicantEmail(application.getUser().getEmail())
            .applicantName(application.getUser().getFullName())
            .businessName(application.getBusinessName())
            .applicantNote(application.getApplicantNote())
            .status(application.getStatus())
            .reviewedBy(application.getReviewedBy() != null ? application.getReviewedBy().getId() : null)
            .reviewedAt(application.getReviewedAt())
            .rejectionReason(application.getRejectionReason())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
    }
}
