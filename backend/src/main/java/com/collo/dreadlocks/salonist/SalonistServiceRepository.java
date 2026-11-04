package com.collo.dreadlocks.salonist;

import com.collo.dreadlocks.service.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalonistServiceRepository
        extends JpaRepository<SalonistServiceLink, SalonistServiceLinkId> {

    @Query("""
        SELECT s
        FROM Service s
        WHERE s.id IN (
            SELECT link.serviceId
            FROM SalonistServiceLink link
            WHERE link.salonistId = :salonistId
        )
        AND s.isActive = true
        ORDER BY s.name
    """)
    List<Service> findActiveServicesBySalonistId(
            @Param("salonistId") Integer salonistId
    );

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
