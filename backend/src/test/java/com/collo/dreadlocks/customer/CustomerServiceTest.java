package com.collo.dreadlocks.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createRejectsDuplicatePhone() {
        when(customerRepository.findByPhone("0711000001"))
                .thenReturn(Optional.of(existingCustomer()));

        CustomerRequest request = new CustomerRequest(
                "New Customer",
                "0711000001",
                null,
                null,
                true
        );

        assertThrows(CustomerConflictException.class, () -> customerService.create(request));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createSavesTrimmedCustomer() {
        when(customerRepository.findByPhone("0711000004")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(4);
            return saved;
        });

        CustomerRequest request = new CustomerRequest(
                "  Jane Moraa  ",
                " 0711000004 ",
                " jane@example.com ",
                LocalDate.of(1992, 3, 14),
                true
        );

        CustomerResponse response = customerService.create(request);

        assertEquals(4, response.id());
        assertEquals("Jane Moraa", response.name());
        assertEquals("0711000004", response.phone());
        assertEquals("jane@example.com", response.email());
    }

    @Test
    void getCustomerThrowsWhenMissing() {
        when(customerRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomer(99));
    }

    @Test
    void upcomingBirthdaysRejectsInvalidDays() {
        assertThrows(InvalidCustomerException.class, () -> customerService.getUpcomingBirthdays(0));
        assertThrows(InvalidCustomerException.class, () -> customerService.getUpcomingBirthdays(367));
    }

    @Test
    void findOrCreateGuestReusesExistingPhone() {
        Customer existing = existingCustomer();
        when(customerRepository.findByPhone("0711000001")).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.findOrCreateGuest("Amina Nyaboke", "0711000001", null);

        assertEquals(1, result.getId());
        assertEquals("Amina Nyaboke", result.getName());
    }

    @Test
    void findOrCreateGuestCreatesWithoutDateOfBirth() {
        when(customerRepository.findByPhone("0799000001")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(9);
            return saved;
        });

        Customer result = customerService.findOrCreateGuest("Guest Client", "0799000001", null);

        assertEquals(9, result.getId());
        assertEquals("Guest Client", result.getName());
        assertEquals("0799000001", result.getPhone());
        assertEquals(null, result.getDateOfBirth());
    }

    private static Customer existingCustomer() {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setName("Amina Nyaboke");
        customer.setPhone("0711000001");
        customer.setActive(true);
        return customer;
    }
}
