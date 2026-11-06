package com.collo.dreadlocks.salonist;

import com.collo.dreadlocks.auth.UserRepository;
import com.collo.dreadlocks.availability.SalonistAvailability;
import com.collo.dreadlocks.availability.SalonistAvailabilityRepository;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.media.MediaStorage;
import com.collo.dreadlocks.notification.NotificationRepository;
import com.collo.dreadlocks.remark.BookingRemarkRepository;
import com.collo.dreadlocks.revenue.ManualWorkRepository;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.service.ServiceRepository;
import com.collo.dreadlocks.unavailability.UnavailabilityRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SalonistCatalogService {

    private static final List<DayOfWeek> WORKING_DAYS = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    private final SalonistRepository salonistRepository;
    private final SalonistServiceRepository salonistServiceRepository;
    private final SalonistAvailabilityRepository availabilityRepository;
    private final ServiceRepository serviceRepository;
    private final MediaStorage mediaStorage;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UnavailabilityRequestRepository unavailabilityRepository;
    private final BookingRepository bookingRepository;
    private final BookingRemarkRepository remarkRepository;
    private final ManualWorkRepository manualWorkRepository;

    public SalonistCatalogService(
            SalonistRepository salonistRepository,
            SalonistServiceRepository salonistServiceRepository,
            SalonistAvailabilityRepository availabilityRepository,
            ServiceRepository serviceRepository,
            MediaStorage mediaStorage,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            UnavailabilityRequestRepository unavailabilityRepository,
            BookingRepository bookingRepository,
            BookingRemarkRepository remarkRepository,
            ManualWorkRepository manualWorkRepository) {
        this.salonistRepository = salonistRepository;
        this.salonistServiceRepository = salonistServiceRepository;
        this.availabilityRepository = availabilityRepository;
        this.serviceRepository = serviceRepository;
        this.mediaStorage = mediaStorage;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.bookingRepository = bookingRepository;
        this.remarkRepository = remarkRepository;
        this.manualWorkRepository = manualWorkRepository;
    }

    @Transactional
    public SalonistResponse create(CreateSalonistRequest request) {
        if (request.serviceIds() == null || request.serviceIds().isEmpty()) {
            throw new InvalidSalonistException("Choose at least one service");
        }
        String email = request.email() == null || request.email().isBlank()
                ? null
                : request.email().trim().toLowerCase();
        if (email != null && !email.contains("@")) {
            throw new InvalidSalonistException("email is invalid");
        }

        LocalDateTime now = LocalDateTime.now();
        Salonist salonist = new Salonist();
        salonist.setName(request.name().trim());
        salonist.setPhone(request.phone().trim());
        salonist.setEmail(email);
        salonist.setBio(request.bio() == null || request.bio().isBlank() ? null : request.bio().trim());
        salonist.setActive(true);
        salonist.setCreatedAt(now);
        salonist.setUpdatedAt(now);
        Salonist saved = salonistRepository.save(salonist);

        for (Integer serviceId : request.serviceIds().stream().distinct().toList()) {
            serviceRepository.findByIdAndIsActiveTrue(serviceId)
                    .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + serviceId));
            SalonistServiceLink link = new SalonistServiceLink();
            link.setSalonistId(saved.getId());
            link.setServiceId(serviceId);
            salonistServiceRepository.save(link);
        }

        for (DayOfWeek day : WORKING_DAYS) {
            SalonistAvailability window = new SalonistAvailability();
            window.setSalonistId(saved.getId());
            window.setDayOfWeek(day);
            window.setStartTime(LocalTime.of(9, 0));
            window.setEndTime(LocalTime.of(17, 0));
            window.setActive(true);
            availabilityRepository.save(window);
        }

        return toResponse(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Salonist salonist = requireSalonistEntity(id);
        if (bookingRepository.existsBySalonistId(id)
                || manualWorkRepository.existsBySalonistId(id)
                || !remarkRepository.findBySalonistIdOrderByCreatedAtDesc(id).isEmpty()) {
            throw new SalonistConflictException(
                    "This salonist has appointments, remarks, or walk-in work, so they stay on the list");
        }
        notificationRepository.deleteAll(notificationRepository.findBySalonistId(id));
        unavailabilityRepository.deleteAll(unavailabilityRepository.findBySalonistIdOrderByStartAtDesc(id));
        userRepository.findBySalonistId(id).ifPresent(userRepository::delete);
        availabilityRepository.deleteAll(availabilityRepository.findBySalonistId(id));
        salonistServiceRepository.deleteAll(salonistServiceRepository.findBySalonistId(id));
        salonistRepository.delete(salonist);
    }

    @Transactional
    public SalonistResponse updatePhoto(Integer id, MultipartFile file) {
        Salonist salonist = requireSalonistEntity(id);
        salonist.setProfileImageUrl(mediaStorage.store(file));
        salonist.setUpdatedAt(LocalDateTime.now());
        return toResponse(salonistRepository.save(salonist));
    }

    public List<SalonistResponse> getActiveSalonists() {
        return salonistRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SalonistResponse> listAll() {
        return salonistRepository.findAllByOrderByNameAsc()
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
        return toResponse(requireSalonistEntity(id));
    }

    public Salonist requireSalonistEntity(Integer id) {
        return salonistRepository.findById(id)
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