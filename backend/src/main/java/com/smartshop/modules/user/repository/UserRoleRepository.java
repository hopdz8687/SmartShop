package com.smartshop.modules.user.repository;

import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserId(Long userId);

    List<UserRole> findByIdRoleId(Long roleId);

    boolean existsByIdUserIdAndIdRoleId(Long userId, Long roleId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role WHERE ur.id.userId = :userId")
    List<UserRole> findWithRoleByUserId(@Param("userId") Long userId);
}
