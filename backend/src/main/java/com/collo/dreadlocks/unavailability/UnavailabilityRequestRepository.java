package com.collo.dreadlocks.unavailability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UnavailabilityRequestRepository
        extends JpaRepository<UnavailabilityRequest, Integer> {

    List<UnavailabilityRequest> findBySalonistIdOrderByStartAtDesc(Integer salonistId);

    List<UnavailabilityRequest> findByStatusOrderByStartAtAsc(UnavailabilityStatus status);

    List<UnavailabilityRequest> findAllByOrderByStartAtDesc();

    Optional<UnavailabilityRequest> findByIdAndSalonistId(Integer id, Integer salonistId);

    List<UnavailabilityRequest> findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Integer salonistId,
            Collection<UnavailabilityStatus> statuses,
            LocalDateTime endAt,
            LocalDateTime startAt
    );
}
