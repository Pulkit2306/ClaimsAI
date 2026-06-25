package com.claimsplatform.aiservice.service;

import com.claimsplatform.aiservice.dto.FraudAnalysisResult;
import com.claimsplatform.common.event.ClaimEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public FraudAnalysisResult analyzeClaim(ClaimEvent claimEvent) {
        String prompt = """
                Analyze the following insurance claim for potential fraud indicators.
                Return your analysis as JSON with these fields:
                - fraudScore (0.0 to 1.0, where 1.0 is definitely fraudulent)
                - riskLevel (LOW, MEDIUM, HIGH, CRITICAL)
                - summary (brief analysis summary)
                - redFlags (list of suspicious indicators found)
                - recommendation (APPROVE, INVESTIGATE, DENY)

                Claim Details:
                - Claim Number: %s
                - Type: %s
                - Description: %s
                - Estimated Amount: %s
                - Status: %s

                Respond ONLY with valid JSON, no other text.
                """.formatted(
                claimEvent.getClaimNumber(),
                claimEvent.getClaimType(),
                claimEvent.getDescription(),
                claimEvent.getEstimatedAmount(),
                claimEvent.getStatus()
        );

        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            Map<String, Object> result = objectMapper.readValue(response, new TypeReference<>() {});

            return FraudAnalysisResult.builder()
                    .claimId(claimEvent.getClaimId())
                    .fraudScore(((Number) result.get("fraudScore")).doubleValue())
                    .riskLevel((String) result.get("riskLevel"))
                    .summary((String) result.get("summary"))
                    .redFlags(objectMapper.convertValue(result.get("redFlags"), new TypeReference<List<String>>() {}))
                    .recommendation((String) result.get("recommendation"))
                    .build();

        } catch (Exception e) {
            log.error("Fraud analysis failed for claim: {}", claimEvent.getClaimNumber(), e);
            return FraudAnalysisResult.builder()
                    .claimId(claimEvent.getClaimId())
                    .fraudScore(0.0)
                    .riskLevel("UNKNOWN")
                    .summary("Analysis failed: " + e.getMessage())
                    .redFlags(List.of())
                    .recommendation("INVESTIGATE")
                    .build();
        }
    }
}
