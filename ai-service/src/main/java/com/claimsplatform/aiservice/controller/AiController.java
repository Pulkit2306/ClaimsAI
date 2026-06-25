package com.claimsplatform.aiservice.controller;

import com.claimsplatform.aiservice.dto.ChatRequest;
import com.claimsplatform.aiservice.dto.ChatResponse;
import com.claimsplatform.aiservice.dto.FraudAnalysisResult;
import com.claimsplatform.aiservice.service.ClaimSummaryService;
import com.claimsplatform.aiservice.service.FraudDetectionService;
import com.claimsplatform.aiservice.service.RagService;
import com.claimsplatform.common.dto.ApiResponse;
import com.claimsplatform.common.enums.ClaimType;
import com.claimsplatform.common.event.ClaimEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final RagService ragService;
    private final FraudDetectionService fraudDetectionService;
    private final ClaimSummaryService claimSummaryService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-User-Email", defaultValue = "anonymous") String userEmail) {
        ChatResponse response = ragService.chat(request, userEmail);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/documents/ingest")
    public ResponseEntity<ApiResponse<String>> ingestDocument(
            @RequestParam Long claimId,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok("Document ingested successfully (demo mode)", null));
    }

    @PostMapping("/fraud/analyze")
    public ResponseEntity<ApiResponse<FraudAnalysisResult>> analyzeFraud(@RequestBody ClaimEvent claimEvent) {
        FraudAnalysisResult result = fraudDetectionService.analyzeClaim(claimEvent);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/claims/summarize")
    public ResponseEntity<ApiResponse<String>> summarizeClaim(
            @RequestParam String description,
            @RequestParam String claimType,
            @RequestParam(required = false) String documents) {
        String summary = claimSummaryService.summarizeClaim(description, claimType, documents);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/claims/assess-value")
    public ResponseEntity<ApiResponse<String>> assessValue(
            @RequestParam String description,
            @RequestParam String claimType,
            @RequestParam(required = false) String historicalData) {
        String assessment = claimSummaryService.assessClaimValue(description, claimType, historicalData);
        return ResponseEntity.ok(ApiResponse.ok(assessment));
    }
}
