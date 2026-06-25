package com.claimsplatform.claimsservice.entity;

import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false, length = 2000)
    private String description;

    private LocalDate incidentDate;

    private BigDecimal estimatedAmount;

    private BigDecimal approvedAmount;

    private String assignedAdjusterId;

    private Double fraudScore;

    private String aiSummary;

    @ElementCollection
    @CollectionTable(name = "claim_documents", joinColumns = @JoinColumn(name = "claim_id"))
    @Column(name = "document_url")
    private List<String> documentUrls;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
