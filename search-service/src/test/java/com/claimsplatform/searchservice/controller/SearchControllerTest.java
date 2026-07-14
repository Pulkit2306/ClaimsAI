package com.claimsplatform.searchservice.controller;

import com.claimsplatform.searchservice.document.AuditLog;
import com.claimsplatform.searchservice.document.ClaimDocument;
import com.claimsplatform.searchservice.service.AuditService;
import com.claimsplatform.searchservice.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "com\\.claimsplatform\\.searchservice\\.config\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SearchService searchService;
    @MockBean AuditService auditService;

    private ClaimDocument autoDoc;
    private ClaimDocument homeDoc;

    @BeforeEach
    void setUp() {
        autoDoc = ClaimDocument.builder()
                .id("1")
                .claimNumber("CLM-AUTO001")
                .claimType("AUTO")
                .status("SUBMITTED")
                .description("Rear-end collision on Highway 40")
                .estimatedAmount(8500.0)
                .fraudScore(0.12)
                .build();

        homeDoc = ClaimDocument.builder()
                .id("2")
                .claimNumber("CLM-HOME001")
                .claimType("HOME")
                .status("APPROVED")
                .description("Kitchen fire caused by faulty wiring")
                .estimatedAmount(35000.0)
                .fraudScore(0.05)
                .build();
    }

    @Test
    @DisplayName("GET /api/search?q= - 200 with matching results")
    void search_validQuery_returns200() throws Exception {
        given(searchService.search(eq("collision"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(autoDoc)));

        mockMvc.perform(get("/api/search").param("q", "collision"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].claimNumber").value("CLM-AUTO001"))
                .andExpect(jsonPath("$.data.content[0].claimType").value("AUTO"));
    }

    @Test
    @DisplayName("GET /api/search?q= - 200 with empty results for no match")
    void search_noMatch_returnsEmptyPage() throws Exception {
        given(searchService.search(eq("xyz123abc"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/search").param("q", "xyz123abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/search?q= - 200 with error message when service throws")
    void search_serviceThrows_returnsErrorMessage() throws Exception {
        given(searchService.search(anyString(), any(Pageable.class)))
                .willThrow(new RuntimeException("Elasticsearch unavailable"));

        mockMvc.perform(get("/api/search").param("q", "fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Search failed: Elasticsearch unavailable"));
    }

    @Test
    @DisplayName("GET /api/search/filter/type/{claimType} - 200 filtered by type")
    void filterByType_returns200() throws Exception {
        given(searchService.filterByType("AUTO")).willReturn(List.of(autoDoc));

        mockMvc.perform(get("/api/search/filter/type/AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].claimType").value("AUTO"))
                .andExpect(jsonPath("$.data[0].claimNumber").value("CLM-AUTO001"));
    }

    @Test
    @DisplayName("GET /api/search/filter/type/{claimType} - 200 empty list for unknown type")
    void filterByType_unknownType_returnsEmpty() throws Exception {
        given(searchService.filterByType("UNKNOWN")).willReturn(List.of());

        mockMvc.perform(get("/api/search/filter/type/UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/search/filter/status/{status} - 200 filtered by status")
    void filterByStatus_returns200() throws Exception {
        given(searchService.filterByStatus("APPROVED")).willReturn(List.of(homeDoc));

        mockMvc.perform(get("/api/search/filter/status/APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.data[0].estimatedAmount").value(35000.0));
    }

    @Test
    @DisplayName("GET /api/search/audit/{entityType}/{entityId} - 200 with audit trail")
    void getAuditHistory_returns200() throws Exception {
        AuditLog log = AuditLog.builder()
                .id("audit-1")
                .eventType("CLAIM_STATUS_CHANGED")
                .entityType("CLAIM")
                .entityId("1")
                .userId("adjuster@intact.ca")
                .action("Status changed to UNDER_REVIEW")
                .timestamp(LocalDateTime.now())
                .build();

        given(auditService.getEntityHistory("CLAIM", "1")).willReturn(List.of(log));

        mockMvc.perform(get("/api/search/audit/CLAIM/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventType").value("CLAIM_STATUS_CHANGED"))
                .andExpect(jsonPath("$.data[0].entityId").value("1"))
                .andExpect(jsonPath("$.data[0].userId").value("adjuster@intact.ca"));
    }

    @Test
    @DisplayName("GET /api/search/audit/user/{userId} - 200 with user activity")
    void getUserActivity_returns200() throws Exception {
        AuditLog log = AuditLog.builder()
                .id("audit-2")
                .eventType("CLAIM_CREATED")
                .entityType("CLAIM")
                .userId("adjuster@desjardins.com")
                .action("New claim submitted")
                .timestamp(LocalDateTime.now())
                .build();

        given(auditService.getUserActivity("adjuster@desjardins.com")).willReturn(List.of(log));

        mockMvc.perform(get("/api/search/audit/user/adjuster@desjardins.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value("adjuster@desjardins.com"))
                .andExpect(jsonPath("$.data[0].eventType").value("CLAIM_CREATED"));
    }
}
