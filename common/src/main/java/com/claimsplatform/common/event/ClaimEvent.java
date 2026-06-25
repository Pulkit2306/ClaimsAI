package com.claimsplatform.common.event;

import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimEvent {

    private String eventType;
    private Long claimId;
    private String claimNumber;
    private Long policyId;
    private Long customerId;
    private ClaimType claimType;
    private ClaimStatus status;
    private BigDecimal estimatedAmount;
    private String description;
    private LocalDateTime occurredAt;
    private LocalDateTime timestamp;
}
