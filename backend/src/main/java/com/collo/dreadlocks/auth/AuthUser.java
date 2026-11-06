package com.collo.dreadlocks.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthUser implements UserDetails {

    private final Integer id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final Integer salonistId;
    private final Integer customerId;
    private final boolean active;

    public AuthUser(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.salonistId = user.getSalonistId();
        this.customerId = user.getCustomerId();
        this.active = user.isActive();
    }

    public Integer getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public Integer getSalonistId() {
        return salonistId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
    public boolean isEnabled() {
        return active;
    }
}
