package com.collo.dreadlocks.availability;

import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.salonist.SalonistResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private SalonistAvailabilityRepository availabilityRepository;

    @Mock
    private SalonistCatalogService salonistCatalogService;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(
                availabilityRepository,
                salonistCatalogService
        );
    }

    @Test
    void createRejectsInvalidTimeRange() {
        when(salonistCatalogService.requireSalonist(1)).thenReturn(salonist());

        AvailabilityRequest request = new AvailabilityRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(17, 0),
                LocalTime.of(9, 0),
                true
        );

        assertThrows(
                InvalidAvailabilityException.class,
                () -> availabilityService.create(1, request)
        );
    }

    @Test
    void createRejectsOverlappingWindow() {
        when(salonistCatalogService.requireSalonist(1)).thenReturn(salonist());

        SalonistAvailability existing = window(
                10,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
        when(availabilityRepository.findBySalonistIdAndDayOfWeekAndIsActiveTrue(
                1,
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing));

        AvailabilityRequest request = new AvailabilityRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(12, 0),
                LocalTime.of(15, 0),
                true
        );

        assertThrows(
                AvailabilityConflictException.class,
                () -> availabilityService.create(1, request)
        );
    }

    @Test
    void createAllowsAdjacentWindow() {
        when(salonistCatalogService.requireSalonist(1)).thenReturn(salonist());

        SalonistAvailability existing = window(
                10,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );
        when(availabilityRepository.findBySalonistIdAndDayOfWeekAndIsActiveTrue(
                1,
                DayOfWeek.MONDAY
        )).thenReturn(List.of(existing));
        when(availabilityRepository.save(any(SalonistAvailability.class)))
                .thenAnswer(invocation -> {
                    SalonistAvailability saved = invocation.getArgument(0);
                    saved.setId(11);
                    return saved;
                });

        AvailabilityRequest request = new AvailabilityRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(12, 0),
                LocalTime.of(17, 0),
                true
        );

        AvailabilityResponse response = availabilityService.create(1, request);

        assertEquals(11, response.id());
        assertEquals(LocalTime.of(12, 0), response.startTime());
        ArgumentCaptor<SalonistAvailability> captor =
                ArgumentCaptor.forClass(SalonistAvailability.class);
        verify(availabilityRepository).save(captor.capture());
        assertEquals(DayOfWeek.MONDAY, captor.getValue().getDayOfWeek());
    }

    @Test
    void updateIgnoresCurrentWindowWhenCheckingOverlap() {
        when(salonistCatalogService.requireSalonist(1)).thenReturn(salonist());

        SalonistAvailability current = window(
                10,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );
        when(availabilityRepository.findByIdAndSalonistId(10, 1))
                .thenReturn(Optional.of(current));
        when(availabilityRepository.findBySalonistIdAndDayOfWeekAndIsActiveTrue(
                1,
                DayOfWeek.MONDAY
        )).thenReturn(List.of(current));
        when(availabilityRepository.save(any(SalonistAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AvailabilityRequest request = new AvailabilityRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(16, 0),
                true
        );

        AvailabilityResponse response = availabilityService.update(1, 10, request);

        assertEquals(LocalTime.of(10, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());
    }

    private static SalonistResponse salonist() {
        return new SalonistResponse(1, "Stylist", "0700000001", null, null, null, true);
    }

    private static SalonistAvailability window(
            Integer id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime) {
        SalonistAvailability availability = new SalonistAvailability();
        availability.setId(id);
        availability.setSalonistId(1);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        availability.setActive(true);
        return availability;
    }
}
