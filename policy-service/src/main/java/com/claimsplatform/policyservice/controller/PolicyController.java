package com.claimsplatform.policyservice.controller;

import com.claimsplatform.common.dto.ApiResponse;
import com.claimsplatform.policyservice.dto.PolicyRequest;
import com.claimsplatform.policyservice.entity.Policy;
import com.claimsplatform.policyservice.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Policy>>> getAllPolicies(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getAllPolicies(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Policy>> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPolicyById(id)));
    }

    @GetMapping("/number/{policyNumber}")
    public ResponseEntity<ApiResponse<Policy>> getPolicyByNumber(@PathVariable String policyNumber) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPolicyByNumber(policyNumber)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Policy>>> getPoliciesByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok(policyService.getPoliciesByCustomer(customerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Policy>> createPolicy(@Valid @RequestBody PolicyRequest request) {
        Policy policy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Policy created", policy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Policy>> updatePolicy(@PathVariable Long id, @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Policy updated", policyService.updatePolicy(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(ApiResponse.ok("Policy deactivated", null));
    }
}
