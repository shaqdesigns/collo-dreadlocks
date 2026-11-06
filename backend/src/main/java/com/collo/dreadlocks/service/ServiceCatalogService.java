
package com.collo.dreadlocks.service;

import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.media.MediaStorage;
import com.collo.dreadlocks.revenue.ManualWorkRepository;
import com.collo.dreadlocks.salonist.SalonistServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final ServicePhotoRepository servicePhotoRepository;
    private final SalonistServiceRepository salonistServiceRepository;
    private final BookingRepository bookingRepository;
    private final ManualWorkRepository manualWorkRepository;
    private final MediaStorage mediaStorage;

    public ServiceCatalogService(
            ServiceRepository serviceRepository,
            ServicePhotoRepository servicePhotoRepository,
            SalonistServiceRepository salonistServiceRepository,
            BookingRepository bookingRepository,
            ManualWorkRepository manualWorkRepository,
            MediaStorage mediaStorage) {
        this.serviceRepository = serviceRepository;
        this.servicePhotoRepository = servicePhotoRepository;
        this.salonistServiceRepository = salonistServiceRepository;
        this.bookingRepository = bookingRepository;
        this.manualWorkRepository = manualWorkRepository;
        this.mediaStorage = mediaStorage;
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

    public List<ServicePhotoResponse> listPhotos(Integer serviceId) {
        requireActive(serviceId);
        return servicePhotoRepository.findByServiceIdOrderByCreatedAtDesc(serviceId)
                .stream()
                .map(this::toPhoto)
                .toList();
    }

    @Transactional
    public ServiceResponse create(ServiceWriteRequest request) {
        LocalDateTime now = LocalDateTime.now();
        com.collo.dreadlocks.service.Service service = new com.collo.dreadlocks.service.Service();
        apply(service, request, null);
        service.setIsActive(true);
        service.setCreatedAt(now);
        service.setUpdatedAt(now);
        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(Integer id, ServiceWriteRequest request) {
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + id));
        apply(service, request, id);
        service.setUpdatedAt(LocalDateTime.now());
        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void delete(Integer id) {
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + id));
        if (bookingRepository.existsByServiceId(id) || manualWorkRepository.existsByServiceId(id)) {
            throw new ServiceConflictException(
                    "This service has bookings or walk-in work, so it stays in the catalogue");
        }
        servicePhotoRepository.deleteAll(servicePhotoRepository.findByServiceIdOrderByCreatedAtDesc(id));
        salonistServiceRepository.deleteAll(salonistServiceRepository.findByServiceId(id));
        serviceRepository.delete(service);
    }

    @Transactional
    public ServicePhotoResponse addPhoto(Integer serviceId, MultipartFile file) {
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));
        ServicePhoto photo = new ServicePhoto();
        photo.setServiceId(serviceId);
        photo.setImageUrl(mediaStorage.store(file));
        ServicePhoto saved = servicePhotoRepository.save(photo);
        service.setImageUrl(saved.getImageUrl());
        service.setUpdatedAt(LocalDateTime.now());
        serviceRepository.save(service);
        return toPhoto(saved);
    }

    private void apply(
            com.collo.dreadlocks.service.Service service,
            ServiceWriteRequest request,
            Integer currentId) {
        String nextName = request.name().trim();
        if (currentId == null || !nextName.equals(service.getName())) {
            service.setSlug(uniqueSlug(nextName, currentId));
        }
        service.setName(nextName);
        service.setCategory(request.category().trim().toUpperCase(Locale.ROOT));
        String description = request.description() == null ? "" : request.description().trim();
        service.setDescription(description.isEmpty() ? null : description);
        service.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        service.setDurationMinutes(request.durationMinutes());
    }

    private String uniqueSlug(String name, Integer currentId) {
        String base = slugify(name);
        String candidate = base;
        for (int suffix = 2; suffix < 50; suffix++) {
            var existing = serviceRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentId)) {
                return candidate;
            }
            candidate = base + "-" + suffix;
        }
        throw new InvalidServiceException("Choose a more distinct service name");
    }

    static String slugify(String name) {
        String slug = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            throw new InvalidServiceException("name needs letters or numbers");
        }
        if (slug.length() > 140) {
            slug = slug.substring(0, 140).replaceAll("-$", "");
        }
        return slug;
    }

    private void requireActive(Integer serviceId) {
        serviceRepository.findByIdAndIsActiveTrue(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));
    }

    private ServicePhotoResponse toPhoto(ServicePhoto photo) {
        return new ServicePhotoResponse(
                photo.getId(),
                photo.getServiceId(),
                photo.getImageUrl(),
                photo.getCreatedAt()
        );
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
