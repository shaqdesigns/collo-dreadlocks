package com.collo.dreadlocks.remark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRemarkRepository extends JpaRepository<BookingRemark, Integer> {

    boolean existsByBookingId(Integer bookingId);

    List<BookingRemark> findAllByOrderByCreatedAtDesc();

    List<BookingRemark> findBySalonistIdOrderByCreatedAtDesc(Integer salonistId);

    List<BookingRemark> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);
}
