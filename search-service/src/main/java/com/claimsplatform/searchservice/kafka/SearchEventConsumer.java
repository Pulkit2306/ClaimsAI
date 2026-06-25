package com.claimsplatform.searchservice.kafka;

import com.claimsplatform.common.event.ClaimEvent;
import com.claimsplatform.searchservice.document.ClaimDocument;
import com.claimsplatform.searchservice.service.AuditService;
import com.claimsplatform.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchEventConsumer {

    private final SearchService searchService;
    private final AuditService auditService;

    @KafkaListener(topics = {"claim.created", "claim.updated", "claim.status.changed"}, groupId = "search-group")
    public void onClaimEvent(ClaimEvent event) {
        log.info("Received {} event for claim: {}", event.getEventType(), event.getClaimNumber());

        ClaimDocument document = ClaimDocument.builder()
                .id(event.getClaimId().toString())
                .claimId(event.getClaimId())
                .claimNumber(event.getClaimNumber())
                .policyId(event.getPolicyId())
                .customerId(event.getCustomerId())
                .claimType(event.getClaimType().name())
                .status(event.getStatus().name())
                .description(event.getDescription())
                .estimatedAmount(event.getEstimatedAmount() != null ? event.getEstimatedAmount().doubleValue() : null)
                .updatedAt(LocalDate.now().toString())
                .build();

        searchService.indexClaim(document);

        auditService.log(
                event.getEventType(),
                "CLAIM",
                event.getClaimId().toString(),
                "SYSTEM",
                event.getEventType(),
                "Claim " + event.getClaimNumber() + " - " + event.getStatus()
        );
    }
}
