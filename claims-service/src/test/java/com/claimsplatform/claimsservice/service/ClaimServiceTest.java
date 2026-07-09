package com.claimsplatform.claimsservice.service;

import com.claimsplatform.claimsservice.dto.ClaimRequest;
import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.claimsservice.kafka.ClaimEventProducer;
import com.claimsplatform.claimsservice.repository.ClaimRepository;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ClaimEventProducer eventProducer;

    @InjectMocks
    private ClaimService claimService;

    private Claim testClaim;
    private ClaimRequest claimRequest;

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
                .incidentDate(LocalDate.now().minusDays(3))
                .estimatedAmount(new BigDecimal("8500.00"))
                .build();

        claimRequest = new ClaimRequest();
        claimRequest.setPolicyId(10L);
        claimRequest.setCustomerId(5L);
        claimRequest.setClaimType("AUTO");
        claimRequest.setDescription("Rear-end collision on Highway 40");
        claimRequest.setIncidentDate(LocalDate.now().minusDays(3));
        claimRequest.setEstimatedAmount(new BigDecimal("8500.00"));
    }

    @Test
    @DisplayName("getAllClaims - returns paginated page")
    void getAllClaims_returnsPaginatedPage() {
        Page<Claim> page = new PageImpl<>(List.of(testClaim));
        given(claimRepository.findAll(any(PageRequest.class))).willReturn(page);

        Page<Claim> result = claimService.getAllClaims(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClaimNumber()).isEqualTo("CLM-ABCD1234");
    }

    @Test
    @DisplayName("getClaimById - existing claim is returned")
    void getClaimById_existing_returnsCllaim() {
        given(claimRepository.findById(1L)).willReturn(Optional.of(testClaim));

        Claim result = claimService.getClaimById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClaimType()).isEqualTo(ClaimType.AUTO);
    }

    @Test
    @DisplayName("getClaimById - missing claim throws ResourceNotFoundException")
    void getClaimById_missing_throws() {
        given(claimRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.getClaimById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createClaim - persists with SUBMITTED status and publishes event")
    void createClaim_persistsAndPublishesEvent() {
        given(claimRepository.save(any(Claim.class))).willAnswer(inv -> {
            Claim c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        Claim result = claimService.createClaim(claimRequest);

        assertThat(result.getClaimNumber()).startsWith("CLM-");
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(result.getClaimType()).isEqualTo(ClaimType.AUTO);
        verify(eventProducer).publishClaimCreated(result);
    }

    @Test
    @DisplayName("updateClaimStatus - changes status and publishes event")
    void updateClaimStatus_changesStatusAndPublishesEvent() {
        given(claimRepository.findById(1L)).willReturn(Optional.of(testClaim));
        given(claimRepository.save(any(Claim.class))).willAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.updateClaimStatus(1L, "UNDER_REVIEW");

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
        verify(eventProducer).publishStatusChanged(result);
    }

    @Test
    @DisplayName("assignAdjuster - sets adjuster id and ADJUSTER_ASSIGNED status")
    void assignAdjuster_setsAdjusterAndStatus() {
        given(claimRepository.findById(1L)).willReturn(Optional.of(testClaim));
        given(claimRepository.save(any(Claim.class))).willAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.assignAdjuster(1L, "adjuster-007");

        assertThat(result.getAssignedAdjusterId()).isEqualTo("adjuster-007");
        assertThat(result.getStatus()).isEqualTo(ClaimStatus.ADJUSTER_ASSIGNED);
        verify(eventProducer).publishClaimUpdated(result);
    }

    @Test
    @DisplayName("getClaimStats - aggregates counts from repository")
    void getClaimStats_aggregatesCounts() {
        given(claimRepository.count()).willReturn(10L);
        given(claimRepository.countByStatus(ClaimStatus.SUBMITTED)).willReturn(4L);
        given(claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW)).willReturn(2L);
        given(claimRepository.countByStatus(ClaimStatus.APPROVED)).willReturn(3L);
        given(claimRepository.countByStatus(ClaimStatus.DENIED)).willReturn(1L);
        given(claimRepository.countByStatus(ClaimStatus.FLAGGED_FRAUD)).willReturn(0L);

        Map<String, Long> stats = claimService.getClaimStats();

        assertThat(stats.get("total")).isEqualTo(10L);
        assertThat(stats.get("submitted")).isEqualTo(4L);
        assertThat(stats.get("approved")).isEqualTo(3L);
    }

    @Test
    @DisplayName("getSuspiciousClaims - delegates threshold to repository")
    void getSuspiciousClaims_delegatesToRepo() {
        Claim fraudClaim = Claim.builder().id(2L).fraudScore(0.95).build();
        given(claimRepository.findSuspiciousClaims(0.7)).willReturn(List.of(fraudClaim));

        List<Claim> result = claimService.getSuspiciousClaims(0.7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFraudScore()).isGreaterThan(0.7);
    }
}
