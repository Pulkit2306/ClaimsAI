package com.claimsplatform.aiservice.kafka;

import com.claimsplatform.aiservice.dto.FraudAnalysisResult;
import com.claimsplatform.aiservice.service.FraudDetectionService;
import com.claimsplatform.common.event.ClaimEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = "claim.created", groupId = "ai-group")
    public void onClaimCreated(ClaimEvent event) {
        log.info("Received claim.created event: {}", event.getClaimNumber());

        FraudAnalysisResult result = fraudDetectionService.analyzeClaim(event);
        log.info("Fraud analysis for claim {}: score={}, risk={}, recommendation={}",
                event.getClaimNumber(), result.getFraudScore(),
                result.getRiskLevel(), result.getRecommendation());
    }
}
