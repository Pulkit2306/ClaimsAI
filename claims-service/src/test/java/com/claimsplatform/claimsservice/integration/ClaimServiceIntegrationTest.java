package com.claimsplatform.claimsservice.integration;

import com.claimsplatform.claimsservice.dto.ClaimRequest;
import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.claimsservice.repository.ClaimRepository;
import com.claimsplatform.claimsservice.service.ClaimService;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ClaimServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("claims_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired private ClaimService claimService;
    @Autowired private ClaimRepository claimRepository;

    @AfterEach
    void tearDown() {
        claimRepository.deleteAll();
    }

    @Test
    @DisplayName("createClaim - persists claim to real PostgreSQL")
    void createClaim_persistsToPostgres() {
        ClaimRequest request = buildRequest("AUTO", "Fender bender in parking lot");

        Claim created = claimService.createClaim(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getClaimNumber()).startsWith("CLM-");
        assertThat(created.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);

        Claim fromDb = claimRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getClaimType()).isEqualTo(ClaimType.AUTO);
        assertThat(fromDb.getDescription()).isEqualTo("Fender bender in parking lot");
    }

    @Test
    @DisplayName("updateClaimStatus - persists status change")
    void updateClaimStatus_persistsChange() {
        ClaimRequest request = buildRequest("HOME", "Kitchen fire caused by faulty wiring");
        Claim claim = claimService.createClaim(request);

        Claim updated = claimService.updateClaimStatus(claim.getId(), "UNDER_REVIEW");

        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
        Claim fromDb = claimRepository.findById(claim.getId()).orElseThrow();
        assertThat(fromDb.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("getClaimStats - returns accurate counts across statuses")
    void getClaimStats_returnAccurateCounts() {
        Claim c1 = claimService.createClaim(buildRequest("AUTO", "Rear-end collision"));
        Claim c2 = claimService.createClaim(buildRequest("HOME", "Burst pipe water damage"));
        claimService.updateClaimStatus(c1.getId(), "APPROVED");

        Map<String, Long> stats = claimService.getClaimStats();

        assertThat(stats.get("total")).isEqualTo(2L);
        assertThat(stats.get("approved")).isEqualTo(1L);
        assertThat(stats.get("submitted")).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAllClaims - pagination works correctly")
    void getAllClaims_paginationWorks() {
        for (int i = 0; i < 5; i++) {
            claimService.createClaim(buildRequest("AUTO", "Claim number " + i));
        }

        Page<Claim> page = claimService.getAllClaims(PageRequest.of(0, 3));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("assignAdjuster - persists adjuster assignment")
    void assignAdjuster_persistsAssignment() {
        Claim claim = claimService.createClaim(buildRequest("LIABILITY", "Slip and fall injury"));

        Claim assigned = claimService.assignAdjuster(claim.getId(), "adjuster-marie-007");

        assertThat(assigned.getAssignedAdjusterId()).isEqualTo("adjuster-marie-007");
        assertThat(assigned.getStatus()).isEqualTo(ClaimStatus.ADJUSTER_ASSIGNED);
    }

    private ClaimRequest buildRequest(String type, String description) {
        ClaimRequest req = new ClaimRequest();
        req.setPolicyId(1L);
        req.setCustomerId(1L);
        req.setClaimType(type);
        req.setDescription(description);
        req.setIncidentDate(LocalDate.now().minusDays(5));
        req.setEstimatedAmount(new BigDecimal("10000.00"));
        return req;
    }
}
