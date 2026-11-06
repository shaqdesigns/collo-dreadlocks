package com.collo.dreadlocks.auth;

import com.collo.dreadlocks.customer.Customer;
import com.collo.dreadlocks.customer.CustomerService;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomerService customerService;
    @Mock
    private SalonistCatalogService salonistCatalogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties();
        properties.getAdmin().setEmail("davidsimiyu341@gmail.com");
        properties.getAdmin().setPassword("DevAdmin123!");
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                customerService,
                salonistCatalogService,
                properties
        );
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());
        assertThrows(
                AuthException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "password"))
        );
    }

    @Test
    void registerRejectsHardcodedAdminEmail() {
        RegisterRequest request = new RegisterRequest(
                "Admin",
                "0700000000",
                "davidsimiyu341@gmail.com",
                "Password123",
                null
        );
        assertThrows(AuthConflictException.class, () -> authService.register(request));
    }

    @Test
    void loginSucceedsForActiveUser() {
        AppUser user = new AppUser();
        user.setId(1);
        user.setEmail("davidsimiyu341@gmail.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
        when(userRepository.findByEmailIgnoreCase("davidsimiyu341@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("DevAdmin123!", "hash")).thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("token");

        AuthResponse response = authService.login(
                new LoginRequest("davidsimiyu341@gmail.com", "DevAdmin123!")
        );

        assertEquals("token", response.token());
        assertEquals(UserRole.ADMIN, response.role());
    }
}
