package com.collo.dreadlocks.remark;

import com.collo.dreadlocks.auth.ForbiddenOperationException;
import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.service.Service;
import com.collo.dreadlocks.service.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemarkServiceTest {

    @Mock
    private BookingRemarkRepository remarkRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private SalonistCatalogService salonistCatalogService;
    @Mock
    private ServiceRepository serviceRepository;

    private RemarkService remarkService;

    @BeforeEach
    void setUp() {
        remarkService = new RemarkService(
                remarkRepository,
                bookingRepository,
                customerService,
                salonistCatalogService,
                serviceRepository
        );
    }

    @Test
    void createRejectsABookingThatIsNotCompleted() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(8)).thenReturn(Optional.of(booking));

        assertThrows(
                InvalidRemarkException.class,
                () -> remarkService.create(4, 8, new RemarkCreateRequest("Careful work"))
        );
    }

    @Test
    void createRejectsAnotherClientsBooking() {
        Booking booking = booking(BookingStatus.COMPLETED);
        when(bookingRepository.findById(8)).thenReturn(Optional.of(booking));

        assertThrows(
                ForbiddenOperationException.class,
                () -> remarkService.create(99, 8, new RemarkCreateRequest("Careful work"))
        );
    }

    @Test
    void createStoresOnePrivateRemark() {
        Booking booking = booking(BookingStatus.COMPLETED);
        when(bookingRepository.findById(8)).thenReturn(Optional.of(booking));
        when(remarkRepository.existsByBookingId(8)).thenReturn(false);
        when(remarkRepository.save(any(BookingRemark.class))).thenAnswer(invocation -> {
            BookingRemark saved = invocation.getArgument(0);
            saved.setId(3);
            saved.setCreatedAt(LocalDateTime.of(2026, 11, 4, 18, 0));
            return saved;
        });
        Customer customer = new Customer();
        customer.setId(4);
        customer.setName("Amina");
        when(customerService.requireCustomer(4)).thenReturn(customer);
        Salonist salonist = new Salonist();
        salonist.setId(1);
        salonist.setName("Collo");
        when(salonistCatalogService.requireSalonistEntity(1)).thenReturn(salonist);
        Service service = new Service();
        service.setId(2);
        service.setName("Dreadlock Retouch");
        when(serviceRepository.findById(2)).thenReturn(Optional.of(service));

        RemarkResponse response = remarkService.create(4, 8, new RemarkCreateRequest("  Careful work  "));

        assertEquals(3, response.id());
        assertEquals("Careful work", response.body());
        assertEquals("Amina", response.customerName());
        assertEquals(1, response.salonistId());
    }

    private Booking booking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(8);
        booking.setCustomerId(4);
        booking.setSalonistId(1);
        booking.setServiceId(2);
        booking.setStatus(status);
        booking.setStartAt(LocalDateTime.of(2026, 11, 4, 9, 0));
        return booking;
    }
}
