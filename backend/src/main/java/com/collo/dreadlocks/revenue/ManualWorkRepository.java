package com.collo.dreadlocks.revenue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualWorkRepository extends JpaRepository<ManualWork, Integer> {

    List<ManualWork> findAllByOrderByWorkedAtDesc();

    List<ManualWork> findBySalonistIdOrderByWorkedAtDesc(Integer salonistId);

    boolean existsByServiceId(Integer serviceId);

    boolean existsBySalonistId(Integer salonistId);
}
