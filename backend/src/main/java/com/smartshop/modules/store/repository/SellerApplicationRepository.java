package com.smartshop.modules.store.repository;

import com.smartshop.modules.store.entity.SellerApplication;
import com.smartshop.modules.store.entity.SellerApplicationStatus;
import com.smartshop.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId, SellerApplicationStatus status);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    List<SellerApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    Page<SellerApplication> findByStatusOrderByCreatedAtDesc(SellerApplicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    Page<SellerApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "reviewedBy"})
    Optional<SellerApplication> findWithUsersById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SellerApplication s
        SET s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.APPROVED,
            s.reviewedBy = :reviewer,
            s.reviewedAt = :reviewedAt,
            s.updatedAt = :reviewedAt
        WHERE s.id = :id
          AND s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.PENDING
        """)
    int approveIfPending(
        @Param("id") Long id,
        @Param("reviewer") User reviewer,
        @Param("reviewedAt") LocalDateTime reviewedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SellerApplication s
        SET s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.REJECTED,
            s.reviewedBy = :reviewer,
            s.reviewedAt = :reviewedAt,
            s.rejectionReason = :reason,
            s.updatedAt = :reviewedAt
        WHERE s.id = :id
          AND s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.PENDING
        """)
    int rejectIfPending(
        @Param("id") Long id,
        @Param("reviewer") User reviewer,
        @Param("reviewedAt") LocalDateTime reviewedAt,
        @Param("reason") String reason
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SellerApplication s
        SET s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.CANCELLED,
            s.updatedAt = :updatedAt
        WHERE s.id = :id
          AND s.user.id = :userId
          AND s.status = com.smartshop.modules.store.entity.SellerApplicationStatus.PENDING
        """)
    int cancelIfPendingAndOwned(
        @Param("id") Long id,
        @Param("userId") Long userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );
}
