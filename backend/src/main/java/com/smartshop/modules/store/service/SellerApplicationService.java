package com.smartshop.modules.store.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.store.dto.CreateSellerApplicationRequest;
import com.smartshop.modules.store.dto.RejectSellerApplicationRequest;
import com.smartshop.modules.store.dto.SellerApplicationResponse;
import com.smartshop.modules.store.entity.SellerApplication;
import com.smartshop.modules.store.entity.SellerApplicationStatus;
import com.smartshop.modules.store.repository.SellerApplicationRepository;
import com.smartshop.modules.store.repository.StoreRepository;
import com.smartshop.modules.user.entity.Role;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserRoleId;
import com.smartshop.modules.user.repository.RoleRepository;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SellerApplicationService {

    private static final String SELLER_ROLE_NAME = "SELLER";

    private final SellerApplicationRepository applicationRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final StoreService storeService;

    public SellerApplicationService(
        SellerApplicationRepository applicationRepository,
        StoreRepository storeRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        StoreService storeService
    ) {
        this.applicationRepository = applicationRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.storeService = storeService;
    }

    @Transactional
    public SellerApplicationResponse submitApplication(Long userId, CreateSellerApplicationRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        if (storeRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("STORE_ALREADY_EXISTS", "You already own a store");
        }

        Role sellerRole = roleRepository.findByName(SELLER_ROLE_NAME)
            .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "SELLER role not found"));
        if (userRoleRepository.existsByIdUserIdAndIdRoleId(userId, sellerRole.getId())) {
            throw new DuplicateResourceException("ALREADY_SELLER", "Account already has SELLER role");
        }

        if (applicationRepository.existsByUserIdAndStatus(userId, SellerApplicationStatus.PENDING)) {
            throw new DuplicateResourceException(
                "PENDING_APPLICATION_EXISTS",
                "You already have a pending seller application"
            );
        }

        String note = request.getApplicantNote() != null ? request.getApplicantNote().trim() : null;
        if (note != null && note.isEmpty()) {
            note = null;
        }

        SellerApplication application = SellerApplication.builder()
            .user(user)
            .businessName(request.getBusinessName().trim())
            .applicantNote(note)
            .status(SellerApplicationStatus.PENDING)
            .build();

        SellerApplication saved = applicationRepository.saveAndFlush(application);
        return SellerApplicationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SellerApplicationResponse> getMyApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(SellerApplicationResponse::from)
            .toList();
    }

    @Transactional
    public SellerApplicationResponse cancelApplication(Long applicationId, Long userId) {
        SellerApplication existing = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));

        if (!existing.getUser().getId().equals(userId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You can only cancel your own seller application"
            );
        }

        int updated = applicationRepository.cancelIfPendingAndOwned(applicationId, userId, LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_APPLICATION_STATE",
                "Only PENDING applications can be cancelled"
            );
        }

        SellerApplication cancelled = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));
        return SellerApplicationResponse.from(cancelled);
    }

    @Transactional(readOnly = true)
    public Page<SellerApplicationResponse> getApplicationsForAdmin(
        SellerApplicationStatus status,
        Pageable pageable
    ) {
        Page<SellerApplication> page = status != null
            ? applicationRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            : applicationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(SellerApplicationResponse::from);
    }

    @Transactional(readOnly = true)
    public SellerApplicationResponse getApplicationByIdForAdmin(Long applicationId) {
        SellerApplication application = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));
        return SellerApplicationResponse.from(application);
    }

    /**
     * Atomically approves a PENDING application, creates the Seller's Store,
     * and grants the SELLER role within a single transaction.
     */
    @Transactional
    public SellerApplicationResponse approveApplication(Long applicationId, Long adminUserId) {
        User reviewer = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Admin user not found"));

        SellerApplication existing = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));

        int updated = applicationRepository.approveIfPending(applicationId, reviewer, LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_APPLICATION_STATE",
                "Only PENDING applications can be approved"
            );
        }

        User applicant = existing.getUser();
        storeService.createStoreForApprovedSeller(applicant, existing.getBusinessName());

        Role sellerRole = roleRepository.findByName(SELLER_ROLE_NAME)
            .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "SELLER role not found"));
        if (!userRoleRepository.existsByIdUserIdAndIdRoleId(applicant.getId(), sellerRole.getId())) {
            UserRole userRole = UserRole.builder()
                .id(new UserRoleId(applicant.getId(), sellerRole.getId()))
                .user(applicant)
                .role(sellerRole)
                .build();
            userRoleRepository.saveAndFlush(userRole);
        }

        SellerApplication approved = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));
        return SellerApplicationResponse.from(approved);
    }

    @Transactional
    public SellerApplicationResponse rejectApplication(
        Long applicationId,
        Long adminUserId,
        RejectSellerApplicationRequest request
    ) {
        User reviewer = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Admin user not found"));

        if (!applicationRepository.existsById(applicationId)) {
            throw new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found");
        }

        int updated = applicationRepository.rejectIfPending(
            applicationId,
            reviewer,
            LocalDateTime.now(),
            request.getRejectionReason().trim()
        );
        if (updated == 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_APPLICATION_STATE",
                "Only PENDING applications can be rejected"
            );
        }

        SellerApplication rejected = applicationRepository.findWithUsersById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("APPLICATION_NOT_FOUND", "Seller application not found"));
        return SellerApplicationResponse.from(rejected);
    }
}
