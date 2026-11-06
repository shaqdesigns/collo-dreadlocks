package com.collo.dreadlocks.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

    List<Service> findByIsActiveTrueOrderByNameAsc();

    Optional<Service> findByIdAndIsActiveTrue(Integer id);

    Optional<Service> findBySlugAndIsActiveTrue(String slug);

    Optional<Service> findBySlug(String slug);

    boolean existsBySlug(String slug);

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
}