package com.collo.dreadlocks.crm;

import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingResponse;
import com.collo.dreadlocks.booking.BookingService;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerRepository;
import com.collo.dreadlocks.customer.CustomerResponse;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.remark.RemarkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CrmService {

    private static final Set<BookingStatus> VISITS = EnumSet.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.COMPLETED
    );

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final RemarkService remarkService;
    private final ClientNoteRepository noteRepository;

    public CrmService(
            CustomerRepository customerRepository,
            CustomerService customerService,
            BookingRepository bookingRepository,
            BookingService bookingService,
            RemarkService remarkService,
            ClientNoteRepository noteRepository) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.remarkService = remarkService;
        this.noteRepository = noteRepository;
    }

    public List<ClientSummaryResponse> list(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase();
        Map<Integer, List<Booking>> byCustomer = bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Booking::getCustomerId));
        LocalDateTime now = LocalDateTime.now();
        return customerRepository.findAllByOrderByNameAsc().stream()
                .filter(customer -> matches(customer, needle))
                .map(customer -> summarize(customer, byCustomer.getOrDefault(customer.getId(), List.of()), now))
                .toList();
    }

    public ClientProfileResponse profile(Integer customerId) {
        CustomerResponse customer = customerService.getCustomer(customerId);
        List<BookingResponse> bookings = bookingService.getByCustomer(customerId);
        List<Booking> rows = bookingRepository.findByCustomerIdOrderByStartAtDesc(customerId);
        LocalDateTime now = LocalDateTime.now();
        ClientSummaryResponse summary = summarize(customerService.requireCustomer(customerId), rows, now);
        List<ClientNoteResponse> notes = noteRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toNote)
                .toList();
        return new ClientProfileResponse(
                customer,
                summary.visits(),
                summary.spent(),
                summary.lastVisitAt(),
                summary.nextVisitAt(),
                bookings,
                remarkService.listForCustomer(customerId),
                notes
        );
    }

    @Transactional
    public ClientNoteResponse addNote(Integer customerId, ClientNoteRequest request) {
        customerService.requireCustomer(customerId);
        ClientNote note = new ClientNote();
        note.setCustomerId(customerId);
        note.setBody(request.body().trim());
        return toNote(noteRepository.save(note));
    }

    private ClientSummaryResponse summarize(Customer customer, List<Booking> bookings, LocalDateTime now) {
        int visits = 0;
        BigDecimal spent = BigDecimal.ZERO;
        LocalDateTime lastVisit = null;
        LocalDateTime nextVisit = null;
        for (Booking booking : bookings) {
            if (!VISITS.contains(booking.getStatus())) {
                continue;
            }
            visits++;
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                spent = spent.add(booking.getPrice());
                if (lastVisit == null || booking.getStartAt().isAfter(lastVisit)) {
                    lastVisit = booking.getStartAt();
                }
            }
            if ((booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED)
                    && !booking.getStartAt().isBefore(now)
                    && (nextVisit == null || booking.getStartAt().isBefore(nextVisit))) {
                nextVisit = booking.getStartAt();
            }
        }
        return new ClientSummaryResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.isActive(),
                visits,
                spent,
                lastVisit,
                nextVisit
        );
    }

    private boolean matches(Customer customer, String needle) {
        if (needle.isEmpty()) {
            return true;
        }
        return customer.getName().toLowerCase().contains(needle)
                || customer.getPhone().toLowerCase().contains(needle)
                || (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(needle));
    }

    private ClientNoteResponse toNote(ClientNote note) {
        return new ClientNoteResponse(note.getId(), note.getCustomerId(), note.getBody(), note.getCreatedAt());
    }
}
