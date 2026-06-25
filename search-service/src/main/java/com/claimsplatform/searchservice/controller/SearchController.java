package com.claimsplatform.searchservice.controller;

import com.claimsplatform.common.dto.ApiResponse;
import com.claimsplatform.searchservice.document.AuditLog;
import com.claimsplatform.searchservice.document.ClaimDocument;
import com.claimsplatform.searchservice.service.AuditService;
import com.claimsplatform.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClaimDocument>>> search(
            @RequestParam String q,
            Pageable pageable) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(searchService.search(q, pageable)));
        } catch (Exception e) {
            log.error("Search failed for query: {}", q, e);
            return ResponseEntity.ok(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/filter/type/{claimType}")
    public ResponseEntity<ApiResponse<List<ClaimDocument>>> filterByType(@PathVariable String claimType) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.filterByType(claimType)));
    }

    @GetMapping("/filter/status/{status}")
    public ResponseEntity<ApiResponse<List<ClaimDocument>>> filterByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.ok(searchService.filterByStatus(status)));
    }

    @GetMapping("/audit/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditHistory(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getEntityHistory(entityType, entityId)));
    }

    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getUserActivity(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(auditService.getUserActivity(userId)));
    }
}
