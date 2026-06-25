package com.claimsplatform.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

    @NotNull
    private Long policyId;

    @NotNull
    private Long customerId;

    @NotBlank
    private String claimType;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate incidentDate;

    private BigDecimal estimatedAmount;

    private List<String> documentUrls;
}
