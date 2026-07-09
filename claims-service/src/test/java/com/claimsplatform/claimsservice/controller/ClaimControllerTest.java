package com.claimsplatform.claimsservice.controller;

import com.claimsplatform.claimsservice.dto.ClaimRequest;
import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.claimsservice.service.ClaimService;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import java.util.Map;

import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClaimController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.claimsplatform\\.claimsservice\\.config\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class ClaimControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ClaimService claimService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Claim testClaim;

    @BeforeEach
    void setUp() {
        testClaim = Claim.builder()
                .id(1L)
                .claimNumber("CLM-ABCD1234")
                .policyId(10L)
                .customerId(5L)
                .claimType(ClaimType.AUTO)
                .status(ClaimStatus.SUBMITTED)
                .description("Rear-end collision on Highway 40")
                .incidentDate(LocalDate.of(2025, 6, 1))
                .estimatedAmount(new BigDecimal("8500.00"))
                .fraudScore(0.12)
                .build();
    }

    @Test
    @DisplayName("GET /api/claims - 200 with paginated list")
    void getAllClaims_returns200() throws Exception {
        given(claimService.getAllClaims(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(testClaim)));

        mockMvc.perform(get("/api/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].claimNumber").value("CLM-ABCD1234"))
                .andExpect(jsonPath("$.data.content[0].status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/claims/{id} - 200 when claim exists")
    void getClaimById_exists_returns200() throws Exception {
        given(claimService.getClaimById(1L)).willReturn(testClaim);

        mockMvc.perform(get("/api/claims/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.claimType").value("AUTO"))
                .andExpect(jsonPath("$.data.estimatedAmount").value(8500.00));
    }

    @Test
    @DisplayName("GET /api/claims/{id} - 404 when claim missing")
    void getClaimById_missing_returns404() throws Exception {
        given(claimService.getClaimById(99L))
                .willThrow(new ResourceNotFoundException("Claim", "id", 99L));

        mockMvc.perform(get("/api/claims/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/claims/number/{claimNumber} - 200 with matching claim")
    void getClaimByNumber_returns200() throws Exception {
        given(claimService.getClaimByNumber("CLM-ABCD1234")).willReturn(testClaim);

        mockMvc.perform(get("/api/claims/number/CLM-ABCD1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-ABCD1234"));
    }

    @Test
    @DisplayName("GET /api/claims/stats - 200 with stats map")
    void getClaimStats_returns200() throws Exception {
        given(claimService.getClaimStats()).willReturn(Map.of(
                "total", 12L, "submitted", 4L, "approved", 3L,
                "underReview", 2L, "denied", 1L, "flaggedFraud", 1L));

        mockMvc.perform(get("/api/claims/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.approved").value(3));
    }

    @Test
    @DisplayName("GET /api/claims/suspicious - 200 with default threshold")
    void getSuspiciousClaims_returns200() throws Exception {
        Claim fraudClaim = Claim.builder().id(2L).fraudScore(0.92).build();
        given(claimService.getSuspiciousClaims(0.7)).willReturn(List.of(fraudClaim));

        mockMvc.perform(get("/api/claims/suspicious"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fraudScore").value(0.92));
    }

    @Test
    @DisplayName("POST /api/claims - 201 with created claim")
    void createClaim_validRequest_returns201() throws Exception {
        ClaimRequest request = new ClaimRequest();
        request.setPolicyId(10L);
        request.setCustomerId(5L);
        request.setClaimType("AUTO");
        request.setDescription("Rear-end collision on Highway 40");
        request.setIncidentDate(LocalDate.of(2025, 6, 1));
        request.setEstimatedAmount(new BigDecimal("8500.00"));

        given(claimService.createClaim(any())).willReturn(testClaim);

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Claim submitted"))
                .andExpect(jsonPath("$.data.claimNumber").value("CLM-ABCD1234"));
    }

    @Test
    @DisplayName("POST /api/claims - 400 when required fields missing")
    void createClaim_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/claims/{id}/status - 200 with updated status")
    void updateStatus_returns200() throws Exception {
        Claim updated = Claim.builder().id(1L).status(ClaimStatus.UNDER_REVIEW).build();
        given(claimService.updateClaimStatus(1L, "UNDER_REVIEW")).willReturn(updated);

        mockMvc.perform(patch("/api/claims/1/status")
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated"))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }

    @Test
    @DisplayName("PATCH /api/claims/{id}/assign - 200 with adjuster assigned")
    void assignAdjuster_returns200() throws Exception {
        Claim assigned = Claim.builder()
                .id(1L).assignedAdjusterId("adj-007")
                .status(ClaimStatus.ADJUSTER_ASSIGNED).build();
        given(claimService.assignAdjuster(1L, "adj-007")).willReturn(assigned);

        mockMvc.perform(patch("/api/claims/1/assign")
                        .param("adjusterId", "adj-007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Adjuster assigned"))
                .andExpect(jsonPath("$.data.assignedAdjusterId").value("adj-007"));
    }

    @Test
    @DisplayName("GET /api/claims/customer/{customerId} - 200 with customer claims")
    void getClaimsByCustomer_returns200() throws Exception {
        given(claimService.getClaimsByCustomer(eq(5L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(testClaim)));

        mockMvc.perform(get("/api/claims/customer/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].customerId").value(5));
    }
}
