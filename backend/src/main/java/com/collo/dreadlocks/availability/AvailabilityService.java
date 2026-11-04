package com.collo.dreadlocks.availability;

import com.collo.dreadlocks.salonist.SalonistCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AvailabilityService {

    private final SalonistAvailabilityRepository availabilityRepository;
    private final SalonistCatalogService salonistCatalogService;

    public AvailabilityService(
            SalonistAvailabilityRepository availabilityRepository,
            SalonistCatalogService salonistCatalogService) {
        this.availabilityRepository = availabilityRepository;
        this.salonistCatalogService = salonistCatalogService;
    }

    public List<AvailabilityResponse> getActiveAvailability(Integer salonistId) {
        salonistCatalogService.getActiveSalonist(salonistId);
        return sort(availabilityRepository.findBySalonistIdAndIsActiveTrue(salonistId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AvailabilityResponse> getAvailabilityForAdmin(Integer salonistId) {
        salonistCatalogService.requireSalonist(salonistId);
        return sort(availabilityRepository.findBySalonistId(salonistId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AvailabilityResponse create(Integer salonistId, AvailabilityRequest request) {
        salonistCatalogService.requireSalonist(salonistId);
        validateRange(request.startTime(), request.endTime());
        boolean isActive = request.isActive() == null || request.isActive();
        if (isActive) {
            ensureNoOverlap(salonistId, request.dayOfWeek(), request.startTime(), request.endTime(), null);
        }

        SalonistAvailability availability = new SalonistAvailability();
        availability.setSalonistId(salonistId);
        apply(availability, request, isActive);
        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public AvailabilityResponse update(
            Integer salonistId,
            Integer availabilityId,
            AvailabilityRequest request) {
        salonistCatalogService.requireSalonist(salonistId);
        validateRange(request.startTime(), request.endTime());

        SalonistAvailability availability = availabilityRepository
                .findByIdAndSalonistId(availabilityId, salonistId)
                .orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

        boolean isActive = request.isActive() == null ? availability.isActive() : request.isActive();
        if (isActive) {
            ensureNoOverlap(
                    salonistId,
                    request.dayOfWeek(),
                    request.startTime(),
                    request.endTime(),
                    availabilityId
            );
        }

        apply(availability, request, isActive);
        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public AvailabilityResponse deactivate(Integer salonistId, Integer availabilityId) {
        salonistCatalogService.requireSalonist(salonistId);
        SalonistAvailability availability = availabilityRepository
                .findByIdAndSalonistId(availabilityId, salonistId)
                .orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));
        availability.setActive(false);
        return toResponse(availabilityRepository.save(availability));
    }

    private void apply(
            SalonistAvailability availability,
            AvailabilityRequest request,
            boolean isActive) {
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setActive(isActive);
    }

    private void validateRange(LocalTime startTime, LocalTime endTime) {
        if (!AvailabilityWindows.isValidRange(startTime, endTime)) {
            throw new InvalidAvailabilityException(
                    "End time must be after start time"
            );
        }
    }

    private void ensureNoOverlap(
            Integer salonistId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Integer excludingId) {
        boolean overlaps = availabilityRepository
                .findBySalonistIdAndDayOfWeekAndIsActiveTrue(salonistId, dayOfWeek)
                .stream()
                .filter(existing -> excludingId == null || !excludingId.equals(existing.getId()))
                .anyMatch(existing -> AvailabilityWindows.overlaps(
                        startTime,
                        endTime,
                        existing.getStartTime(),
                        existing.getEndTime()
                ));

        if (overlaps) {
            throw new AvailabilityConflictException(
                    "Availability overlaps an existing window for this salonist and day"
            );
        }
    }

    private List<SalonistAvailability> sort(List<SalonistAvailability> windows) {
        return windows.stream()
                .sorted(Comparator
                        .comparing(SalonistAvailability::getDayOfWeek)
                        .thenComparing(SalonistAvailability::getStartTime))
                .toList();
    }

    private AvailabilityResponse toResponse(SalonistAvailability availability) {
        return new AvailabilityResponse(
                availability.getId(),
                availability.getSalonistId(),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.isActive()
        );
    }
}
