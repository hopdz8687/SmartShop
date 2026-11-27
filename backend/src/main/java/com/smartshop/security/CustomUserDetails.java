package com.smartshop.security;

import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final boolean deleted;
    private final List<String> roleNames;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user, List<String> roleNames) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.status = user.getStatus();
        this.deleted = user.getDeletedAt() != null;
        this.roleNames = List.copyOf(roleNames);
        this.authorities = roleNames.stream()
            .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !deleted && status == UserStatus.ACTIVE;
    }
}
