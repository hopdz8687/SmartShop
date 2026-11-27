package com.smartshop.modules.user.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.user.dto.UserResponse;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserStatus;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(
        UserRepository userRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        if (user.getDeletedAt() != null || user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE", "Account is not active");
        }

        List<String> roles = userRoleRepository.findWithRoleByUserId(user.getId())
            .stream()
            .map(ur -> ur.getRole().getName())
            .toList();

        return UserResponse.from(user, roles);
    }
}
