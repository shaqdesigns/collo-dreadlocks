package com.collo.dreadlocks.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    public AdminUserBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = authProperties.getAdmin().getEmail().trim().toLowerCase();
        AppUser admin = userRepository.findByEmailIgnoreCase(email).orElseGet(AppUser::new);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(authProperties.getAdmin().getPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setSalonistId(null);
        admin.setCustomerId(null);
        admin.setActive(true);
        userRepository.save(admin);
    }
}
