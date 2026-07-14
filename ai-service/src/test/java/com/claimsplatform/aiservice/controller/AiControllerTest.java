package com.claimsplatform.aiservice.controller;

import com.claimsplatform.aiservice.dto.ChatRequest;
import com.claimsplatform.aiservice.dto.ChatResponse;
import com.claimsplatform.aiservice.dto.FraudAnalysisResult;
import com.claimsplatform.aiservice.service.ClaimSummaryService;
import com.claimsplatform.aiservice.service.FraudDetectionService;
import com.claimsplatform.aiservice.service.RagService;
import com.claimsplatform.common.enums.ClaimStatus;
import com.claimsplatform.common.enums.ClaimType;
import com.claimsplatform.common.event.ClaimEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.claimsplatform\\.aiservice\\.config\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RagService ragService;
    @MockBean FraudDetectionService fraudDetectionService;
    @MockBean ClaimSummaryService claimSummaryService;

    @Test
    @DisplayName("POST /api/ai/chat - 200 with AI response")
    void chat_validRequest_returns200() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("Show me fraud analysis");

        ChatResponse chatResponse = ChatResponse.builder()
                .response("Fraud detected with 0.85 risk score")
                .sessionId("sess-abc123")
                .sourceDocs(List.of("Demo Mode"))
                .build();

        given(ragService.chat(any(), anyString())).willReturn(chatResponse);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Email", "adjuster@intact.ca")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.response").value("Fraud detected with 0.85 risk score"))
                .andExpect(jsonPath("$.data.sessionId").value("sess-abc123"));
    }

    @Test
    @DisplayName("POST /api/ai/chat - uses anonymous when no X-User-Email header")
    void chat_noEmailHeader_usesAnonymous() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("How does claims processing work?");

        given(ragService.chat(any(), eq("anonymous"))).willReturn(
                ChatResponse.builder().response("Process info").sessionId("s1").sourceDocs(List.of()).build());

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.response").value("Process info"));
    }

    @Test
    @DisplayName("POST /api/ai/fraud/analyze - 200 with fraud result")
    void analyzeFraud_returns200() throws Exception {
        ClaimEvent event = ClaimEvent.builder()
                .claimId(1L)
                .claimNumber("CLM-TEST001")
                .claimType(ClaimType.LIABILITY)
                .status(ClaimStatus.SUBMITTED)
                .estimatedAmount(new BigDecimal("120000"))
                .description("Warehouse accident")
                .build();

        FraudAnalysisResult result = FraudAnalysisResult.builder()
                .claimId(1L)
                .fraudScore(0.87)
                .riskLevel("HIGH")
                .summary("Multiple red flags detected")
                .redFlags(List.of("high amount", "vague description"))
                .recommendation("INVESTIGATE")
                .build();

        given(fraudDetectionService.analyzeClaim(any())).willReturn(result);

        mockMvc.perform(post("/api/ai/fraud/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fraudScore").value(0.87))
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.redFlags[0]").value("high amount"))
                .andExpect(jsonPath("$.data.recommendation").value("INVESTIGATE"));
    }

    @Test
    @DisplayName("POST /api/ai/claims/summarize - 200 with summary text")
    void summarizeClaim_returns200() throws Exception {
        given(claimSummaryService.summarizeClaim(anyString(), anyString(), any()))
                .willReturn("AI Summary (auto): Rear-end collision. Priority: MEDIUM.");

        mockMvc.perform(post("/api/ai/claims/summarize")
                        .param("description", "Rear-end collision on Highway 40")
                        .param("claimType", "AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("AI Summary (auto): Rear-end collision. Priority: MEDIUM."));
    }

    @Test
    @DisplayName("POST /api/ai/claims/assess-value - 200 with value assessment")
    void assessValue_returns200() throws Exception {
        given(claimSummaryService.assessClaimValue(anyString(), anyString(), any()))
                .willReturn("Estimated value range: $8,000 - $25,000");

        mockMvc.perform(post("/api/ai/claims/assess-value")
                        .param("description", "Roof damage from hail storm")
                        .param("claimType", "HOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Estimated value range: $8,000 - $25,000"));
    }

    @Test
    @DisplayName("POST /api/ai/documents/ingest - 200 in demo mode")
    void ingestDocument_returns200() throws Exception {
        mockMvc.perform(post("/api/ai/documents/ingest")
                        .param("claimId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
