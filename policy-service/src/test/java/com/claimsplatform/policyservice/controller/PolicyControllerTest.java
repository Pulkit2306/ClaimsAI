package com.claimsplatform.policyservice.controller;

import com.claimsplatform.common.enums.PolicyType;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import com.claimsplatform.policyservice.dto.PolicyRequest;
import com.claimsplatform.policyservice.entity.Customer;
import com.claimsplatform.policyservice.entity.Policy;
import com.claimsplatform.policyservice.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PolicyController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.claimsplatform\\.policyservice\\.config\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean PolicyService policyService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Policy testPolicy;
    private PolicyRequest validRequest;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .id(1L).firstName("Marie").lastName("Tremblay")
                .email("marie@example.com").build();

        testPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-ABC12345")
                .policyType(PolicyType.AUTO)
                .customer(customer)
                .premiumAmount(new BigDecimal("1200.00"))
                .coverageAmount(new BigDecimal("50000.00"))
                .deductible(new BigDecimal("500.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .active(true)
                .build();

        validRequest = new PolicyRequest();
        validRequest.setCustomerId(1L);
        validRequest.setPolicyType("AUTO");
        validRequest.setPremiumAmount(new BigDecimal("1200.00"));
        validRequest.setCoverageAmount(new BigDecimal("50000.00"));
        validRequest.setDeductible(new BigDecimal("500.00"));
        validRequest.setStartDate(LocalDate.of(2025, 1, 1));
        validRequest.setEndDate(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("GET /api/policies - 200 with paginated list")
    void getAllPolicies_returns200() throws Exception {
        given(policyService.getAllPolicies(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(testPolicy)));

        mockMvc.perform(get("/api/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].policyNumber").value("POL-ABC12345"))
                .andExpect(jsonPath("$.data.content[0].policyType").value("AUTO"));
    }

    @Test
    @DisplayName("GET /api/policies/{id} - 200 when policy exists")
    void getPolicyById_exists_returns200() throws Exception {
        given(policyService.getPolicyById(1L)).willReturn(testPolicy);

        mockMvc.perform(get("/api/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.premiumAmount").value(1200.00))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("GET /api/policies/{id} - 404 when policy not found")
    void getPolicyById_missing_returns404() throws Exception {
        given(policyService.getPolicyById(99L))
                .willThrow(new ResourceNotFoundException("Policy", "id", 99L));

        mockMvc.perform(get("/api/policies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/policies/number/{policyNumber} - 200 with matching policy")
    void getPolicyByNumber_returns200() throws Exception {
        given(policyService.getPolicyByNumber("POL-ABC12345")).willReturn(testPolicy);

        mockMvc.perform(get("/api/policies/number/POL-ABC12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.policyNumber").value("POL-ABC12345"));
    }

    @Test
    @DisplayName("GET /api/policies/customer/{customerId} - 200 with customer policies")
    void getPoliciesByCustomer_returns200() throws Exception {
        given(policyService.getPoliciesByCustomer(1L)).willReturn(List.of(testPolicy));

        mockMvc.perform(get("/api/policies/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].policyType").value("AUTO"));
    }

    @Test
    @DisplayName("POST /api/policies - 201 with created policy")
    void createPolicy_validRequest_returns201() throws Exception {
        given(policyService.createPolicy(any())).willReturn(testPolicy);

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Policy created"))
                .andExpect(jsonPath("$.data.policyNumber").value("POL-ABC12345"));
    }

    @Test
    @DisplayName("POST /api/policies - 400 when required fields missing")
    void createPolicy_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/policies - 400 when premium amount is negative")
    void createPolicy_negativeAmount_returns400() throws Exception {
        validRequest.setPremiumAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/policies/{id} - 200 with updated policy")
    void updatePolicy_validRequest_returns200() throws Exception {
        Policy updated = Policy.builder().id(1L).policyType(PolicyType.HOME)
                .premiumAmount(new BigDecimal("999.00")).build();
        given(policyService.updatePolicy(eq(1L), any())).willReturn(updated);

        validRequest.setPolicyType("HOME");
        validRequest.setPremiumAmount(new BigDecimal("999.00"));

        mockMvc.perform(put("/api/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Policy updated"))
                .andExpect(jsonPath("$.data.policyType").value("HOME"));
    }

    @Test
    @DisplayName("DELETE /api/policies/{id} - 200 soft deletes policy")
    void deletePolicy_returns200() throws Exception {
        mockMvc.perform(delete("/api/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Policy deactivated"));
    }

    @Test
    @DisplayName("DELETE /api/policies/{id} - 404 when policy not found")
    void deletePolicy_missing_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Policy", "id", 99L))
                .when(policyService).deletePolicy(99L);

        mockMvc.perform(delete("/api/policies/99"))
                .andExpect(status().isNotFound());
    }
}
