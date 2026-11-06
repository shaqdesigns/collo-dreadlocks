package com.collo.dreadlocks.unavailability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnavailabilityServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-04T08:00:00Z"),
            ZoneId.of("Africa/Nairobi")
    );

    @Mock
    private UnavailabilityRequestRepository requestRepository;

    private UnavailabilityService unavailabilityService;

    @BeforeEach
    void setUp() {
        unavailabilityService = new UnavailabilityService(requestRepository, CLOCK);
    }

    @Test
    void createRejectsEndBeforeStart() {
        UnavailabilityCreateRequest request = new UnavailabilityCreateRequest(
                LocalDateTime.of(2026, 11, 10, 17, 0),
                LocalDateTime.of(2026, 11, 10, 9, 0),
                "Busy"
        );
        assertThrows(
                InvalidUnavailabilityException.class,
                () -> unavailabilityService.create(1, request)
        );
    }

    @Test
    void createRejectsOverlappingPendingRequest() {
        UnavailabilityRequest existing = new UnavailabilityRequest();
        existing.setId(3);
        existing.setStartAt(LocalDateTime.of(2026, 11, 10, 9, 0));
        existing.setEndAt(LocalDateTime.of(2026, 11, 10, 17, 0));
        when(requestRepository.findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                eq(1), any(), any(), any()
        )).thenReturn(List.of(existing));

        UnavailabilityCreateRequest request = new UnavailabilityCreateRequest(
                LocalDateTime.of(2026, 11, 10, 12, 0),
                LocalDateTime.of(2026, 11, 10, 15, 0),
                "Busy"
        );

        assertThrows(
                UnavailabilityConflictException.class,
                () -> unavailabilityService.create(1, request)
        );
    }

    @Test
    void createSavesPendingRequest() {
        when(requestRepository.findBySalonistIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                eq(1), any(), any(), any()
        )).thenReturn(List.of());
        when(requestRepository.save(any(UnavailabilityRequest.class))).thenAnswer(invocation -> {
            UnavailabilityRequest saved = invocation.getArgument(0);
            saved.setId(9);
            return saved;
        });

        UnavailabilityResponse response = unavailabilityService.create(
                1,
                new UnavailabilityCreateRequest(
                        LocalDateTime.of(2026, 11, 10, 9, 0),
                        LocalDateTime.of(2026, 11, 10, 13, 0),
                        "Clinic"
                )
        );

        assertEquals(9, response.id());
        assertEquals(UnavailabilityStatus.PENDING, response.status());
    }
}
