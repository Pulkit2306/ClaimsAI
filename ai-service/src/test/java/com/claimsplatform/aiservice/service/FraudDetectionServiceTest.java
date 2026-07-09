package com.claimsplatform.aiservice.service;

import com.claimsplatform.aiservice.dto.FraudAnalysisResult;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import com.claimsplatform.common.event.ClaimEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock private ChatClient.CallResponseSpec callSpec;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ClaimEvent testEvent;

    @BeforeEach
    void setUp() throws Exception {
        var field = FraudDetectionService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(fraudDetectionService, objectMapper);

        testEvent = ClaimEvent.builder()
                .claimId(1L)
                .claimNumber("CLM-TEST001")
                .claimType(ClaimType.LIABILITY)
                .description("Warehouse accident with multiple witnesses")
                .estimatedAmount(new BigDecimal("120000.00"))
                .status(ClaimStatus.SUBMITTED)
                .build();
    }

    @Test
    @DisplayName("analyzeClaim - parses valid AI JSON response")
    void analyzeClaim_parsesValidJsonResponse() throws Exception {
        String mockJson = """
                {
                  "fraudScore": 0.82,
                  "riskLevel": "HIGH",
                  "summary": "Multiple fraud indicators detected",
                  "redFlags": ["unusually high amount", "vague description"],
                  "recommendation": "INVESTIGATE"
                }
                """;

        given(chatClientBuilder.build()).willReturn(chatClient);
        given(chatClient.prompt()).willReturn(requestSpec);
        given(requestSpec.user(any(String.class))).willReturn(requestSpec);
        given(requestSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn(mockJson);

        FraudAnalysisResult result = fraudDetectionService.analyzeClaim(testEvent);

        assertThat(result.getClaimId()).isEqualTo(1L);
        assertThat(result.getFraudScore()).isEqualTo(0.82);
        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.getRedFlags()).hasSize(2);
        assertThat(result.getRecommendation()).isEqualTo("INVESTIGATE");
    }

    @Test
    @DisplayName("analyzeClaim - returns safe default when AI call throws")
    void analyzeClaim_aiThrows_returnsSafeDefault() {
        given(chatClientBuilder.build()).willThrow(new RuntimeException("AI unavailable"));

        FraudAnalysisResult result = fraudDetectionService.analyzeClaim(testEvent);

        assertThat(result.getClaimId()).isEqualTo(1L);
        assertThat(result.getFraudScore()).isEqualTo(0.0);
        assertThat(result.getRiskLevel()).isEqualTo("UNKNOWN");
        assertThat(result.getRecommendation()).isEqualTo("INVESTIGATE");
        assertThat(result.getRedFlags()).isEmpty();
    }

    @Test
    @DisplayName("analyzeClaim - returns safe default when AI returns invalid JSON")
    void analyzeClaim_invalidJson_returnsSafeDefault() {
        given(chatClientBuilder.build()).willReturn(chatClient);
        given(chatClient.prompt()).willReturn(requestSpec);
        given(requestSpec.user(any(String.class))).willReturn(requestSpec);
        given(requestSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn("This is not JSON at all");

        FraudAnalysisResult result = fraudDetectionService.analyzeClaim(testEvent);

        assertThat(result.getRiskLevel()).isEqualTo("UNKNOWN");
        assertThat(result.getFraudScore()).isEqualTo(0.0);
    }
}
