package com.smartshop.security;

import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.repository.UserRepository;
import com.smartshop.modules.user.repository.UserRoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads active, non-deleted users and their current roles from the database.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(
        UserRepository userRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public CustomUserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildUserDetails(user);
    }

    private CustomUserDetails buildUserDetails(User user) {
        List<String> roleNames = userRoleRepository.findWithRoleByUserId(user.getId())
            .stream()
            .map(ur -> ur.getRole().getName())
            .toList();
        return new CustomUserDetails(user, roleNames);
    }
}
