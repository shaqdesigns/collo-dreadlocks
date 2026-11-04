
package com.collo.dreadlocks.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceResponse> getActiveServices() {
        return serviceRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ServiceResponse getActiveService(Integer id) {
        return serviceRepository.findByIdAndIsActiveTrue(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ServiceNotFoundException("Service not found: " + id)
                );
    }

    public List<ServiceResponse> getServicesForSalonist(Integer salonistId) {
        return serviceRepository.findActiveServicesBySalonistId(salonistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ServiceResponse toResponse(
            com.collo.dreadlocks.service.Service service) {

        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getSlug(),
                service.getCategory(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.getImageUrl(),
                service.getIsActive()
        );
    }
}
