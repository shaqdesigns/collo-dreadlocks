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

    @Query(value = """
        SELECT s.*
        FROM services s
        INNER JOIN salonist_services ss
            ON ss.service_id = s.id
        WHERE ss.salonist_id = :salonistId
          AND s.is_active = 1
        ORDER BY s.name
        """, nativeQuery = true)
    List<Service> findActiveServicesBySalonistId(
            @Param("salonistId") Integer salonistId
    );
}