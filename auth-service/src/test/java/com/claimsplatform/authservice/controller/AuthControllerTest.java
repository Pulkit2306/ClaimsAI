package com.claimsplatform.authservice.controller;

import com.claimsplatform.authservice.service.AuthService;
import com.claimsplatform.common.dto.AuthRequest;
import com.claimsplatform.common.dto.AuthResponse;
import com.claimsplatform.common.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = com.claimsplatform.authservice.config.SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockAuthResponse = AuthResponse.builder()
                .accessToken("test-jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400)
                .role("ADJUSTER")
                .fullName("Marie Tremblay")
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register - 201 with token on valid request")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Marie", "Tremblay", "marie@desjardins.com", "password123", "ADJUSTER");

        given(authService.register(any())).willReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.role").value("ADJUSTER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 400 when required fields missing")
    void register_missingFields_returns400() throws Exception {
        String badBody = """
                { "firstName": "", "email": "not-an-email", "password": "short" }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - 400 when email already registered")
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Marie", "Tremblay", "existing@example.com", "password123", null);

        given(authService.register(any()))
                .willThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 200 with token on valid credentials")
    void login_validCredentials_returns200() throws Exception {
        AuthRequest request = new AuthRequest("marie@desjardins.com", "password123");

        given(authService.login(any())).willReturn(mockAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.fullName").value("Marie Tremblay"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 400 on wrong password")
    void login_wrongPassword_returns400() throws Exception {
        AuthRequest request = new AuthRequest("marie@desjardins.com", "wrongpass");

        given(authService.login(any()))
                .willThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 400 when body is empty")
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
