package com.collo.dreadlocks.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySalonistId(Integer salonistId);

    Optional<AppUser> findBySalonistId(Integer salonistId);

    boolean existsByCustomerId(Integer customerId);

    Optional<AppUser> findByCustomerId(Integer customerId);
}
