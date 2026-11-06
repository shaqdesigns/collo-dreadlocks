package com.collo.dreadlocks.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicePhotoRepository extends JpaRepository<ServicePhoto, Integer> {

    List<ServicePhoto> findByServiceIdOrderByCreatedAtDesc(Integer serviceId);
}
