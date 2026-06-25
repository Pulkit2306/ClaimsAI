package com.claimsplatform.claimsservice.repository;

import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    Page<Claim> findByCustomerId(Long customerId, Pageable pageable);
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
    Page<Claim> findByClaimType(ClaimType claimType, Pageable pageable);
    List<Claim> findByPolicyId(Long policyId);
    long countByStatus(ClaimStatus status);

    @Query("SELECT c FROM Claim c WHERE c.fraudScore IS NOT NULL AND c.fraudScore > :threshold ORDER BY c.fraudScore DESC")
    List<Claim> findSuspiciousClaims(Double threshold);
}
