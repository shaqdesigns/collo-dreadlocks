package com.collo.dreadlocks.auth;

import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.salonist.AdminSalonistResponse;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.salonist.SalonistResponse;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomerService customerService;
    private final SalonistCatalogService salonistCatalogService;
    private final AuthProperties authProperties;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomerService customerService,
            SalonistCatalogService salonistCatalogService,
            AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customerService = customerService;
        this.salonistCatalogService = salonistCatalogService;
        this.authProperties = authProperties;
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        return toResponse(user, jwtService.createToken(user));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (email.equalsIgnoreCase(authProperties.getAdmin().getEmail())) {
            throw new AuthConflictException("This email cannot be used to register");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthConflictException("An account with this email already exists");
        }

        Customer customer = customerService.registerAccount(
                request.name(),
                request.phone(),
                email,
                request.dateOfBirth()
        );
        if (userRepository.existsByCustomerId(customer.getId())) {
            throw new AuthConflictException("This customer already has a portal account");
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setCustomerId(customer.getId());
        user.setActive(true);
        return toResponse(userRepository.save(user), jwtService.createToken(user));
    }

    public List<AdminSalonistResponse> listSalonists() {
        return salonistCatalogService.listAll()
                .stream()
                .map(this::toAdminSalonist)
                .toList();
    }

    @Transactional
    public AuthResponse createSalonistAccess(Integer salonistId, SalonistAccessRequest request) {
        Salonist salonist = salonistCatalogService.requireSalonistEntity(salonistId);
        String email = request.email().trim().toLowerCase();
        if (email.equalsIgnoreCase(authProperties.getAdmin().getEmail())) {
            throw new AuthConflictException("This email is reserved for the administrator");
        }

        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!salonistId.equals(existing.getSalonistId())) {
                throw new AuthConflictException("An account with this email already exists");
            }
        });

        salonist.setEmail(email);
        AppUser user = userRepository.findBySalonistId(salonistId).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setRole(UserRole.SALONIST);
            created.setSalonistId(salonistId);
            return created;
        });

        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.SALONIST);
        user.setSalonistId(salonistId);
        user.setCustomerId(null);
        user.setActive(true);
        return toResponse(userRepository.save(user), jwtService.createToken(user));
    }

    public AuthResponse me(AuthUser current) {
        AppUser user = userRepository.findById(current.getId())
                .orElseThrow(() -> new AuthException("User not found"));
        return toResponse(user, jwtService.createToken(user));
    }

    private AdminSalonistResponse toAdminSalonist(SalonistResponse salonist) {
        return new AdminSalonistResponse(
                salonist.id(),
                salonist.name(),
                salonist.phone(),
                salonist.email(),
                salonist.bio(),
                salonist.isActive(),
                userRepository.existsBySalonistId(salonist.id())
        );
    }

    private AuthResponse toResponse(AppUser user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getSalonistId(),
                user.getCustomerId()
        );
    }
}
