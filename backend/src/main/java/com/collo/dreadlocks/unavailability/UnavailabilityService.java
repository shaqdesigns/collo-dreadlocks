package com.collo.dreadlocks.unavailability;

import com.collo.dreadlocks.booking.BookingSlots;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class UnavailabilityService {

    private static final Set<UnavailabilityStatus> BLOCKING_OR_PENDING =
            EnumSet.of(UnavailabilityStatus.PENDING, UnavailabilityStatus.APPROVED);

    private static final Set<UnavailabilityStatus> APPROVED =
            EnumSet.of(UnavailabilityStatus.APPROVED);

    private final UnavailabilityRequestRepository requestRepository;
    private final Clock clock;

    public UnavailabilityService(
            UnavailabilityRequestRepository requestRepository,
            Clock clock) {
        this.requestRepository = requestRepository;
        this.clock = clock;
    }

    public List<UnavailabilityResponse> listForSalonist(Integer salonistId) {
        return requestRepository.findBySalonistIdOrderByStartAtDesc(salonistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UnavailabilityResponse> listForAdmin(UnavailabilityStatus status) {
        List<UnavailabilityRequest> requests = status == null
                ? requestRepository.findAllByOrderByStartAtDesc()
                : requestRepository.findByStatusOrderByStartAtAsc(status);
        return requests.stream().map(this::toResponse).toList();
    }

    public List<UnavailabilityRequest> approvedOverlapping(
            Integer salonistId,
            LocalDateTime startAt,
            LocalDateTime endAt) {
        return requestRepository
                .findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        salonistId,
                        APPROVED,
                        endAt,
                        startAt
                );
    }

    @Transactional
    public UnavailabilityResponse create(Integer salonistId, UnavailabilityCreateRequest request) {
        validateRange(request.startAt(), request.endAt());
        ensureNoOverlap(salonistId, request.startAt(), request.endAt(), null);

        UnavailabilityRequest entity = new UnavailabilityRequest();
        entity.setSalonistId(salonistId);
        entity.setStartAt(request.startAt());
        entity.setEndAt(request.endAt());
        entity.setReason(request.reason() == null || request.reason().isBlank()
                ? null
                : request.reason().trim());
        entity.setStatus(UnavailabilityStatus.PENDING);
        return toResponse(requestRepository.save(entity));
    }

    @Transactional
    public UnavailabilityResponse cancel(Integer salonistId, Integer id) {
        UnavailabilityRequest entity = requestRepository.findByIdAndSalonistId(id, salonistId)
                .orElseThrow(() -> new UnavailabilityNotFoundException(id));
        if (entity.getStatus() != UnavailabilityStatus.PENDING) {
            throw new InvalidUnavailabilityException("Only pending requests can be cancelled");
        }
        entity.setStatus(UnavailabilityStatus.CANCELLED);
        return toResponse(requestRepository.save(entity));
    }

    @Transactional
    public UnavailabilityResponse approve(Integer id, UnavailabilityReviewRequest review) {
        return review(id, UnavailabilityStatus.APPROVED, review);
    }

    @Transactional
    public UnavailabilityResponse reject(Integer id, UnavailabilityReviewRequest review) {
        return review(id, UnavailabilityStatus.REJECTED, review);
    }

    private UnavailabilityResponse review(
            Integer id,
            UnavailabilityStatus target,
            UnavailabilityReviewRequest review) {
        UnavailabilityRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new UnavailabilityNotFoundException(id));
        if (entity.getStatus() != UnavailabilityStatus.PENDING) {
            throw new InvalidUnavailabilityException("Only pending requests can be reviewed");
        }
        entity.setStatus(target);
        entity.setReviewedAt(LocalDateTime.now(clock));
        if (review != null && review.adminNote() != null && !review.adminNote().isBlank()) {
            entity.setAdminNote(review.adminNote().trim());
        }
        return toResponse(requestRepository.save(entity));
    }

    private void validateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new InvalidUnavailabilityException("End time must be after start time");
        }
        if (endAt.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidUnavailabilityException("Unavailability cannot end in the past");
        }
    }

    private void ensureNoOverlap(
            Integer salonistId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Integer excludingId) {
        boolean overlaps = requestRepository
                .findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        salonistId,
                        BLOCKING_OR_PENDING,
                        endAt,
                        startAt
                )
                .stream()
                .filter(existing -> excludingId == null || !excludingId.equals(existing.getId()))
                .anyMatch(existing -> BookingSlots.overlaps(
                        startAt,
                        endAt,
                        existing.getStartAt(),
                        existing.getEndAt()
                ));
        if (overlaps) {
            throw new UnavailabilityConflictException(
                    "This period overlaps another pending or approved unavailability request"
            );
        }
    }

    private UnavailabilityResponse toResponse(UnavailabilityRequest entity) {
        return new UnavailabilityResponse(
                entity.getId(),
                entity.getSalonistId(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getReason(),
                entity.getStatus(),
                entity.getAdminNote()
        );
    }
}
