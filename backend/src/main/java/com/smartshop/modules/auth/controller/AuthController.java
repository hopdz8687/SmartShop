package com.smartshop.modules.auth.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.auth.dto.AuthResponse;
import com.smartshop.modules.auth.dto.LoginRequest;
import com.smartshop.modules.auth.dto.RegisterRequest;
import com.smartshop.modules.auth.service.AuthService;
import com.smartshop.modules.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * Stateless JWT logout: instructs the client to discard its access token.
     * Server-side token revocation/blacklist is not implemented in MVP.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(
            ApiResponse.ok("Logged out (stateless JWT: please discard the token on the client)", null)
        );
    }
}
