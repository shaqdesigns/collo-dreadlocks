package com.collo.dreadlocks.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByBookingId(Integer bookingId);

    Optional<Payment> findByCheckoutRequestId(String checkoutRequestId);
}
