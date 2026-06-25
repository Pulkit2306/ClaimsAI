package com.claimsplatform.claimsservice.kafka;

import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.common.event.ClaimEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimEventProducer {

    private final KafkaTemplate<String, ClaimEvent> kafkaTemplate;

    public static final String TOPIC_CLAIM_CREATED = "claim.created";
    public static final String TOPIC_CLAIM_UPDATED = "claim.updated";
    public static final String TOPIC_CLAIM_STATUS_CHANGED = "claim.status.changed";

    public void publishClaimCreated(Claim claim) {
        ClaimEvent event = buildEvent("CLAIM_CREATED", claim);
        kafkaTemplate.send(TOPIC_CLAIM_CREATED, claim.getClaimNumber(), event);
        log.info("Published claim.created event for claim: {}", claim.getClaimNumber());
    }

    public void publishClaimUpdated(Claim claim) {
        ClaimEvent event = buildEvent("CLAIM_UPDATED", claim);
        kafkaTemplate.send(TOPIC_CLAIM_UPDATED, claim.getClaimNumber(), event);
        log.info("Published claim.updated event for claim: {}", claim.getClaimNumber());
    }

    public void publishStatusChanged(Claim claim) {
        ClaimEvent event = buildEvent("CLAIM_STATUS_CHANGED", claim);
        kafkaTemplate.send(TOPIC_CLAIM_STATUS_CHANGED, claim.getClaimNumber(), event);
        log.info("Published claim.status.changed event for claim: {} -> {}", claim.getClaimNumber(), claim.getStatus());
    }

    private ClaimEvent buildEvent(String eventType, Claim claim) {
        return ClaimEvent.builder()
                .eventType(eventType)
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .customerId(claim.getCustomerId())
                .claimType(claim.getClaimType())
                .status(claim.getStatus())
                .estimatedAmount(claim.getEstimatedAmount())
                .description(claim.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
