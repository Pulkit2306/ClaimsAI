package com.claimsplatform.claimsservice.controller;

import com.claimsplatform.claimsservice.dto.ClaimRequest;
import com.claimsplatform.claimsservice.entity.Claim;
import com.claimsplatform.claimsservice.service.ClaimService;
import com.claimsplatform.common.dto.ApiResponse;
import com.claimsplatform.common.enums.ClaimStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Claim>>> getAllClaims(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getAllClaims(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Claim>> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimById(id)));
    }

    @GetMapping("/number/{claimNumber}")
    public ResponseEntity<ApiResponse<Claim>> getClaimByNumber(@PathVariable String claimNumber) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimByNumber(claimNumber)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<Claim>>> getClaimsByCustomer(@PathVariable Long customerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimsByCustomer(customerId, pageable)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<Claim>>> getClaimsByStatus(@PathVariable ClaimStatus status, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimsByStatus(status, pageable)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getClaimStats() {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getClaimStats()));
    }

    @GetMapping("/suspicious")
    public ResponseEntity<ApiResponse<List<Claim>>> getSuspiciousClaims(@RequestParam(defaultValue = "0.7") Double threshold) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.getSuspiciousClaims(threshold)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Claim>> createClaim(@Valid @RequestBody ClaimRequest request) {
        Claim claim = claimService.createClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Claim submitted", claim));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Claim>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", claimService.updateClaimStatus(id, status)));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Claim>> assignAdjuster(@PathVariable Long id, @RequestParam String adjusterId) {
        return ResponseEntity.ok(ApiResponse.ok("Adjuster assigned", claimService.assignAdjuster(id, adjusterId)));
    }
}
