package com.claimsplatform.policyservice.service;

import com.claimsplatform.common.enums.PolicyType;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import com.claimsplatform.policyservice.dto.PolicyRequest;
import com.claimsplatform.policyservice.entity.Customer;
import com.claimsplatform.policyservice.entity.Policy;
import com.claimsplatform.policyservice.repository.CustomerRepository;
import com.claimsplatform.policyservice.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock private PolicyRepository policyRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private PolicyService policyService;

    private Customer customer;
    private Policy policy;
    private PolicyRequest policyRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("Marie")
                .lastName("Tremblay")
                .email("marie@example.com")
                .build();

        policy = Policy.builder()
                .id(1L)
                .policyNumber("POL-ABC12345")
                .policyType(PolicyType.AUTO)
                .customer(customer)
                .premiumAmount(new BigDecimal("1200.00"))
                .coverageAmount(new BigDecimal("50000.00"))
                .deductible(new BigDecimal("500.00"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .active(true)
                .build();

        policyRequest = new PolicyRequest();
        policyRequest.setCustomerId(1L);
        policyRequest.setPolicyType("AUTO");
        policyRequest.setPremiumAmount(new BigDecimal("1200.00"));
        policyRequest.setCoverageAmount(new BigDecimal("50000.00"));
        policyRequest.setDeductible(new BigDecimal("500.00"));
        policyRequest.setStartDate(LocalDate.now());
        policyRequest.setEndDate(LocalDate.now().plusYears(1));
    }

    @Test
    @DisplayName("getAllPolicies - returns paginated results")
    void getAllPolicies_returnsPaginatedResults() {
        Page<Policy> page = new PageImpl<>(List.of(policy));
        given(policyRepository.findAll(any(PageRequest.class))).willReturn(page);

        Page<Policy> result = policyService.getAllPolicies(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPolicyNumber()).isEqualTo("POL-ABC12345");
    }

    @Test
    @DisplayName("getPolicyById - existing id returns policy")
    void getPolicyById_existingId_returnsPolicy() {
        given(policyRepository.findById(1L)).willReturn(Optional.of(policy));

        Policy result = policyService.getPolicyById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPolicyType()).isEqualTo(PolicyType.AUTO);
    }

    @Test
    @DisplayName("getPolicyById - missing id throws ResourceNotFoundException")
    void getPolicyById_missing_throws() {
        given(policyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.getPolicyById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createPolicy - assigns generated policy number and saves")
    void createPolicy_savesWithGeneratedNumber() {
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(policyRepository.save(any(Policy.class))).willAnswer(inv -> inv.getArgument(0));

        Policy result = policyService.createPolicy(policyRequest);

        assertThat(result.getPolicyNumber()).startsWith("POL-");
        assertThat(result.getPolicyType()).isEqualTo(PolicyType.AUTO);
        assertThat(result.isActive()).isTrue();
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    @DisplayName("createPolicy - missing customer throws ResourceNotFoundException")
    void createPolicy_missingCustomer_throws() {
        given(customerRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.createPolicy(policyRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updatePolicy - updates fields and saves")
    void updatePolicy_updatesFields() {
        given(policyRepository.findById(1L)).willReturn(Optional.of(policy));
        given(policyRepository.save(any(Policy.class))).willAnswer(inv -> inv.getArgument(0));

        policyRequest.setPolicyType("HOME");
        policyRequest.setPremiumAmount(new BigDecimal("999.00"));
        Policy result = policyService.updatePolicy(1L, policyRequest);

        assertThat(result.getPolicyType()).isEqualTo(PolicyType.HOME);
        assertThat(result.getPremiumAmount()).isEqualByComparingTo("999.00");
    }

    @Test
    @DisplayName("deletePolicy - soft deletes by setting active=false")
    void deletePolicy_softDelete() {
        given(policyRepository.findById(1L)).willReturn(Optional.of(policy));
        given(policyRepository.save(any(Policy.class))).willAnswer(inv -> inv.getArgument(0));

        policyService.deletePolicy(1L);

        assertThat(policy.isActive()).isFalse();
        verify(policyRepository).save(policy);
    }

    @Test
    @DisplayName("getPoliciesByCustomer - delegates to repository")
    void getPoliciesByCustomer_delegatesToRepo() {
        given(policyRepository.findByCustomerId(1L)).willReturn(List.of(policy));

        List<Policy> result = policyService.getPoliciesByCustomer(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomer().getEmail()).isEqualTo("marie@example.com");
    }
}
