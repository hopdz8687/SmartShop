package com.smartshop.modules.user.dto;

import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Safe public user representation; never includes {@code passwordHash}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private UserStatus status;
    private Boolean emailVerified;
    private List<String> roles;

    public static UserResponse from(User user, List<String> roles) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .avatarUrl(user.getAvatarUrl())
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .roles(List.copyOf(roles))
            .build();
    }
}
