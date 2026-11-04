package com.collo.dreadlocks.salonist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonistRepository extends JpaRepository<Salonist, Integer> {

    List<Salonist> findByIsActiveTrueOrderByNameAsc();

    Optional<Salonist> findByIdAndIsActiveTrue(Integer id);

    @Query("""
        SELECT s
        FROM Salonist s
        WHERE s.id IN (
            SELECT link.salonistId
            FROM SalonistServiceLink link
            WHERE link.serviceId = :serviceId
        )
        AND s.isActive = true
        ORDER BY s.name
    """)
    List<Salonist> findActiveSalonistsByServiceId(
            @Param("serviceId") Integer serviceId
    );
}
