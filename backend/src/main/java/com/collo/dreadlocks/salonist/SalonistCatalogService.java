package com.collo.dreadlocks.salonist;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalonistCatalogService {

    private final SalonistRepository salonistRepository;

    public SalonistCatalogService(SalonistRepository salonistRepository) {
        this.salonistRepository = salonistRepository;
    }

    public List<SalonistResponse> getActiveSalonists() {
        return salonistRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SalonistResponse getActiveSalonist(Integer id) {
        return salonistRepository.findByIdAndIsActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new SalonistNotFoundException(id));
    }

    public SalonistResponse requireSalonist(Integer id) {
        return salonistRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new SalonistNotFoundException(id));
    }

    public List<SalonistResponse> getSalonistsForService(Integer serviceId) {
        return salonistRepository.findActiveSalonistsByServiceId(serviceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SalonistResponse toResponse(Salonist salonist) {
        return new SalonistResponse(
                salonist.getId(),
                salonist.getName(),
                salonist.getPhone(),
                salonist.getEmail(),
                salonist.getBio(),
                salonist.getProfileImageUrl(),
                salonist.isActive()
        );
    }
}