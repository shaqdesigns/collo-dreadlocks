package com.collo.dreadlocks.customer;

import com.collo.dreadlocks.booking.BookingResponse;
import com.collo.dreadlocks.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final CustomerService customerService;
    private final BookingService bookingService;

    public AdminCustomerController(
            CustomerService customerService,
            BookingService bookingService) {
        this.customerService = customerService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String phone) {
        return ResponseEntity.ok(customerService.getCustomers(phone));
    }

    @GetMapping("/birthdays")
    public ResponseEntity<List<CustomerResponse>> getUpcomingBirthdays(
            @RequestParam(required = false) Integer days) {
        return ResponseEntity.ok(customerService.getUpcomingBirthdays(days));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<BookingResponse>> getCustomerBookings(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getByCustomer(id));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomerResponse> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.deactivate(id));
    }
}
