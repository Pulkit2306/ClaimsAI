package com.claimsplatform.authservice.service;

import com.claimsplatform.authservice.entity.User;
import com.claimsplatform.authservice.repository.UserRepository;
import com.claimsplatform.common.dto.AuthRequest;
import com.claimsplatform.common.dto.AuthResponse;
import com.claimsplatform.common.dto.RegisterRequest;
import com.claimsplatform.common.enums.UserRole;
import com.claimsplatform.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("$2a$10$encodedPassword")
                .role(UserRole.ADJUSTER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("register - success with default POLICYHOLDER role")
    void register_defaultRole_success() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPassword("password123");

        given(userRepository.existsByEmail("jane@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$hashed");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        given(jwtUtil.generateToken(anyString(), anyString(), anyMap())).willReturn("jwt-token-abc");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token-abc");
        assertThat(response.getRole()).isEqualTo("POLICYHOLDER");
        assertThat(response.getFullName()).isEqualTo("Jane Smith");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - explicit role is honoured")
    void register_explicitRole_honoured() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Admin");
        request.setEmail("alice@example.com");
        request.setPassword("secure");
        request.setRole("ADMIN");

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("hashed");
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(jwtUtil.generateToken(anyString(), eq("ADMIN"), anyMap())).willReturn("admin-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("register - throws when email already exists")
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("pass");

        given(userRepository.existsByEmail("john.doe@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }

    @Test
    @DisplayName("login - valid credentials return token")
    void login_validCredentials_returnsToken() {
        AuthRequest request = new AuthRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("rawPassword");

        given(userRepository.findByEmail("john.doe@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("rawPassword", testUser.getPassword())).willReturn(true);
        given(jwtUtil.generateToken(anyString(), anyString(), anyMap())).willReturn("valid-jwt");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("valid-jwt");
        assertThat(response.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("login - unknown email throws")
    void login_unknownEmail_throws() {
        AuthRequest request = new AuthRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("pass");

        given(userRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("login - wrong password throws")
    void login_wrongPassword_throws() {
        AuthRequest request = new AuthRequest();
        request.setEmail("john.doe@example.com");
        request.setPassword("wrongPass");

        given(userRepository.findByEmail("john.doe@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPass", testUser.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
    }
}
