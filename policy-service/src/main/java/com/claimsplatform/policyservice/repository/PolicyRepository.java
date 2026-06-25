package com.claimsplatform.policyservice.repository;

import com.claimsplatform.common.enums.PolicyType;
import com.claimsplatform.policyservice.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByCustomerId(Long customerId);
    Page<Policy> findByPolicyType(PolicyType policyType, Pageable pageable);
    Page<Policy> findByActiveTrue(Pageable pageable);
    long countByActive(boolean active);
}
