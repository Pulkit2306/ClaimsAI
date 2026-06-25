package com.claimsplatform.aiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAnalysisResult {

    private Long claimId;
    private double fraudScore;
    private String riskLevel;
    private String summary;
    private List<String> redFlags;
    private String recommendation;
}
