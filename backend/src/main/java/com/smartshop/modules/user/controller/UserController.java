package com.smartshop.modules.user.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.user.dto.UserResponse;
import com.smartshop.modules.user.service.UserService;
import com.smartshop.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserResponse response = userService.getCurrentUser(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Current user retrieved", response));
    }
}
