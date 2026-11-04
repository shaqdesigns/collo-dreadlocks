package com.collo.dreadlocks.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    List<Customer> findAllByOrderByNameAsc();

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);

    List<Customer> findByDateOfBirthIsNotNullAndIsActiveTrue();
}
