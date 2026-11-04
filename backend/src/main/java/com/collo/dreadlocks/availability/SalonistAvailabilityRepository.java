package com.collo.dreadlocks.availability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface SalonistAvailabilityRepository
        extends JpaRepository<SalonistAvailability, Integer> {

    List<SalonistAvailability> findBySalonistIdAndIsActiveTrue(Integer salonistId);

    List<SalonistAvailability> findBySalonistId(Integer salonistId);

    List<SalonistAvailability> findBySalonistIdAndDayOfWeekAndIsActiveTrue(
            Integer salonistId,
            DayOfWeek dayOfWeek
    );

    Optional<SalonistAvailability> findByIdAndSalonistId(Integer id, Integer salonistId);
}
