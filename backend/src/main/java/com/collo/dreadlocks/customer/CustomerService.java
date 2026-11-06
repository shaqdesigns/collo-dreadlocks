package com.collo.dreadlocks.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private static final int DEFAULT_BIRTHDAY_DAYS = 7;
    private static final int MAX_BIRTHDAY_DAYS = 366;

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> getCustomers(String phone) {
        if (phone != null && !phone.isBlank()) {
            return customerRepository.findByPhone(phone.trim())
                    .map(this::toResponse)
                    .map(List::of)
                    .orElseGet(List::of);
        }

        return customerRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomer(Integer id) {
        return toResponse(requireCustomer(id));
    }

    public Customer requireCustomer(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public Customer registerAccount(
            String name,
            String phone,
            String email,
            LocalDate dateOfBirth) {
        String normalizedPhone = phone.trim();
        String normalizedName = name.trim();
        String normalizedEmail = normalizeEmail(email);

        return customerRepository.findByPhone(normalizedPhone)
                .map(existing -> {
                    existing.setName(normalizedName);
                    if (normalizedEmail != null) {
                        existing.setEmail(normalizedEmail);
                    }
                    if (existing.getDateOfBirth() == null && dateOfBirth != null) {
                        existing.setDateOfBirth(dateOfBirth);
                    }
                    existing.setActive(true);
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setName(normalizedName);
                    customer.setPhone(normalizedPhone);
                    customer.setEmail(normalizedEmail);
                    customer.setDateOfBirth(dateOfBirth);
                    customer.setActive(true);
                    return customerRepository.save(customer);
                });
    }

    @Transactional
    public Customer findOrCreateGuest(String name, String phone, String email) {
        String normalizedPhone = phone == null ? null : phone.trim();
        String normalizedName = name == null ? null : name.trim();
        String normalizedEmail = normalizeEmail(email);

        return customerRepository.findByPhone(normalizedPhone)
                .map(existing -> {
                    existing.setName(normalizedName);
                    if (existing.getEmail() == null && normalizedEmail != null) {
                        existing.setEmail(normalizedEmail);
                    }
                    existing.setActive(true);
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setName(normalizedName);
                    customer.setPhone(normalizedPhone);
                    customer.setEmail(normalizedEmail);
                    customer.setActive(true);
                    return customerRepository.save(customer);
                });
    }

    public List<CustomerResponse> getUpcomingBirthdays(Integer days) {
        int windowDays = days == null ? DEFAULT_BIRTHDAY_DAYS : days;
        if (windowDays < 1 || windowDays > MAX_BIRTHDAY_DAYS) {
            throw new InvalidCustomerException(
                    "days must be between 1 and " + MAX_BIRTHDAY_DAYS
            );
        }

        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(windowDays);

        return customerRepository.findByDateOfBirthIsNotNullAndIsActiveTrue()
                .stream()
                .filter(customer -> BirthdayWindow.fallsWithin(
                        customer.getDateOfBirth(),
                        from,
                        to
                ))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String phone = normalizePhone(request.phone());
        if (customerRepository.findByPhone(phone).isPresent()) {
            throw new CustomerConflictException("A customer with this phone already exists");
        }

        Customer customer = new Customer();
        apply(customer, request, phone, request.isActive() == null || request.isActive());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Integer id, CustomerRequest request) {
        Customer customer = requireCustomer(id);
        String phone = normalizePhone(request.phone());
        if (customerRepository.existsByPhoneAndIdNot(phone, id)) {
            throw new CustomerConflictException("A customer with this phone already exists");
        }

        boolean isActive = request.isActive() == null ? customer.isActive() : request.isActive();
        apply(customer, request, phone, isActive);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse deactivate(Integer id) {
        Customer customer = requireCustomer(id);
        customer.setActive(false);
        return toResponse(customerRepository.save(customer));
    }

    private void apply(
            Customer customer,
            CustomerRequest request,
            String phone,
            boolean isActive) {
        customer.setName(request.name().trim());
        customer.setPhone(phone);
        customer.setEmail(normalizeEmail(request.email()));
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setActive(isActive);
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getDateOfBirth(),
                customer.isActive()
        );
    }
}
