package com.claimsplatform.claimsservice.service;

import com.claimsplatform.claimsservice.dto.ClaimRequest;
import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.claimsservice.kafka.ClaimEventProducer;
import com.claimsplatform.claimsservice.repository.ClaimRepository;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimEventProducer eventProducer;

    public Page<Claim> getAllClaims(Pageable pageable) {
        return claimRepository.findAll(pageable);
    }

    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", id));
    }

    public Claim getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "claimNumber", claimNumber));
    }

    public Page<Claim> getClaimsByCustomer(Long customerId, Pageable pageable) {
        return claimRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Claim> getClaimsByStatus(ClaimStatus status, Pageable pageable) {
        return claimRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Claim createClaim(ClaimRequest request) {
        Claim claim = Claim.builder()
                .claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .policyId(request.getPolicyId())
                .customerId(request.getCustomerId())
                .claimType(ClaimType.valueOf(request.getClaimType().toUpperCase()))
                .status(ClaimStatus.SUBMITTED)
                .description(request.getDescription())
                .incidentDate(request.getIncidentDate())
                .estimatedAmount(request.getEstimatedAmount())
                .documentUrls(request.getDocumentUrls())
                .build();

        claim = claimRepository.save(claim);
        eventProducer.publishClaimCreated(claim);
        return claim;
    }

    @Transactional
    public Claim updateClaimStatus(Long id, String newStatus) {
        Claim claim = getClaimById(id);
        claim.setStatus(ClaimStatus.valueOf(newStatus.toUpperCase()));
        claim = claimRepository.save(claim);
        eventProducer.publishStatusChanged(claim);
        return claim;
    }

    @Transactional
    public Claim assignAdjuster(Long id, String adjusterId) {
        Claim claim = getClaimById(id);
        claim.setAssignedAdjusterId(adjusterId);
        claim.setStatus(ClaimStatus.ADJUSTER_ASSIGNED);
        claim = claimRepository.save(claim);
        eventProducer.publishClaimUpdated(claim);
        return claim;
    }

    public Map<String, Long> getClaimStats() {
        return Map.of(
                "total", claimRepository.count(),
                "submitted", claimRepository.countByStatus(ClaimStatus.SUBMITTED),
                "underReview", claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW),
                "approved", claimRepository.countByStatus(ClaimStatus.APPROVED),
                "denied", claimRepository.countByStatus(ClaimStatus.DENIED),
                "flaggedFraud", claimRepository.countByStatus(ClaimStatus.FLAGGED_FRAUD)
        );
    }

    public List<Claim> getSuspiciousClaims(Double threshold) {
        return claimRepository.findSuspiciousClaims(threshold);
    }
}
