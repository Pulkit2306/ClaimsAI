package com.claimsplatform.policyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {

    @NotNull
    private Long customerId;

    @NotBlank
    private String policyType;

    @NotNull
    @Positive
    private BigDecimal premiumAmount;

    @NotNull
    @Positive
    private BigDecimal coverageAmount;

    @NotNull
    @Positive
    private BigDecimal deductible;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String description;
}
