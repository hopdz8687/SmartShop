package com.smartshop.modules.auth.service;

import com.smartshop.common.exception.DuplicateResourceException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.auth.dto.AuthResponse;
import com.smartshop.modules.auth.dto.LoginRequest;
import com.smartshop.modules.auth.dto.RegisterRequest;
import com.smartshop.modules.user.dto.UserResponse;
import com.smartshop.modules.user.entity.Role;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserRole;
import com.smartshop.modules.user.entity.UserRoleId;
import com.smartshop.modules.user.entity.UserStatus;
import com.smartshop.modules.user.repository.RoleRepository;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import com.smartshop.security.CustomUserDetails;
import com.smartshop.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private static final String DEFAULT_CUSTOMER_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new Customer account.
     * UserStatus defaults to ACTIVE per DDL v2.1 (email verification is not part of MVP).
     * Concurrent duplicate emails are caught via the database UNIQUE constraint on users.email.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS", "Email is already registered");
        }

        Role customerRole = roleRepository.findByName(DEFAULT_CUSTOMER_ROLE)
            .orElseThrow(() -> new ResourceNotFoundException("ROLE_NOT_FOUND", "Default CUSTOMER role not found"));

        User user = User.builder()
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName().trim())
            .phone(request.getPhone() != null ? request.getPhone().trim() : null)
            .status(UserStatus.ACTIVE)
            .emailVerified(Boolean.FALSE)
            .build();

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("EMAIL_ALREADY_EXISTS", "Email is already registered");
        }

        UserRole userRole = UserRole.builder()
            .id(new UserRoleId(savedUser.getId(), customerRole.getId()))
            .user(savedUser)
            .role(customerRole)
            .build();
        userRoleRepository.save(userRole);

        return UserResponse.from(savedUser, List.of(customerRole.getName()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        String token = jwtService.generateToken(
            userDetails.getUserId(),
            userDetails.getEmail(),
            userDetails.getRoleNames()
        );

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationMs())
            .user(UserResponse.from(user, userDetails.getRoleNames()))
            .build();
    }
}
