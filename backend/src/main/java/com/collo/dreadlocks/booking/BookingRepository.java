package com.collo.dreadlocks.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    Optional<Booking> findByReference(UUID reference);

    List<Booking> findByCustomerIdOrderByStartAtDesc(Integer customerId);

    List<Booking> findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Integer salonistId,
            Collection<BookingStatus> statuses,
            LocalDateTime endAt,
            LocalDateTime startAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.salonistId = :salonistId
              AND b.status IN :statuses
              AND b.startAt < :endAt
              AND :startAt < b.endAt
            """)
    List<Booking> lockOverlapping(
            @Param("salonistId") Integer salonistId,
            @Param("statuses") Collection<BookingStatus> statuses,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            SELECT b
            FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:fromInclusive IS NULL OR b.startAt >= :fromInclusive)
              AND (:toExclusive IS NULL OR b.startAt < :toExclusive)
            ORDER BY b.startAt ASC
            """)
    List<Booking> search(
            @Param("status") BookingStatus status,
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}
