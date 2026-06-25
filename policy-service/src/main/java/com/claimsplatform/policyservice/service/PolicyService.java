package com.claimsplatform.policyservice.service;

import com.claimsplatform.common.enums.PolicyType;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import com.claimsplatform.policyservice.dto.PolicyRequest;
import com.claimsplatform.policyservice.entity.Customer;
import com.claimsplatform.policyservice.entity.Policy;
import com.claimsplatform.policyservice.repository.CustomerRepository;
import com.claimsplatform.policyservice.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;

    public Page<Policy> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable);
    }

    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", id));
    }

    public Policy getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "policyNumber", policyNumber));
    }

    public List<Policy> getPoliciesByCustomer(Long customerId) {
        return policyRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Policy createPolicy(PolicyRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Policy policy = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .policyType(PolicyType.valueOf(request.getPolicyType().toUpperCase()))
                .customer(customer)
                .premiumAmount(request.getPremiumAmount())
                .coverageAmount(request.getCoverageAmount())
                .deductible(request.getDeductible())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .active(true)
                .build();

        return policyRepository.save(policy);
    }

    @Transactional
    public Policy updatePolicy(Long id, PolicyRequest request) {
        Policy policy = getPolicyById(id);
        policy.setPolicyType(PolicyType.valueOf(request.getPolicyType().toUpperCase()));
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setDeductible(request.getDeductible());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
        policy.setDescription(request.getDescription());
        return policyRepository.save(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policy.setActive(false);
        policyRepository.save(policy);
    }
}
