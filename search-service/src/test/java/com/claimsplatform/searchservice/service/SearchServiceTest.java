package com.claimsplatform.searchservice.service;

import com.claimsplatform.searchservice.document.ClaimDocument;
import com.claimsplatform.searchservice.repository.ClaimSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock private ClaimSearchRepository claimSearchRepository;
    @Mock private ElasticsearchOperations elasticsearchOperations;
    @Mock private SearchHits<ClaimDocument> searchHits;
    @Mock private SearchHit<ClaimDocument> searchHit;

    @InjectMocks
    private SearchService searchService;

    private ClaimDocument autoDoc;
    private ClaimDocument homeDoc;

    @BeforeEach
    void setUp() {
        autoDoc = ClaimDocument.builder()
                .id("1").claimNumber("CLM-AUTO001")
                .claimType("AUTO").status("SUBMITTED")
                .description("Rear-end collision on Highway 40")
                .estimatedAmount(8500.0).build();

        homeDoc = ClaimDocument.builder()
                .id("2").claimNumber("CLM-HOME001")
                .claimType("HOME").status("APPROVED")
                .description("Kitchen fire caused by faulty wiring")
                .estimatedAmount(35000.0).build();
    }

    @Test
    @DisplayName("search - maps ES hits to Page of ClaimDocuments")
    void search_mapsHitsToPage() {
        given(searchHit.getContent()).willReturn(autoDoc);
        given(searchHits.getSearchHits()).willReturn(List.of(searchHit));
        given(searchHits.getTotalHits()).willReturn(1L);
        given(elasticsearchOperations.search(any(), eq(ClaimDocument.class))).willReturn(searchHits);

        var page = searchService.search("collision", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getClaimNumber()).isEqualTo("CLM-AUTO001");
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("search - returns empty page when no hits")
    void search_noHits_returnsEmptyPage() {
        given(searchHits.getSearchHits()).willReturn(List.of());
        given(searchHits.getTotalHits()).willReturn(0L);
        given(elasticsearchOperations.search(any(), eq(ClaimDocument.class))).willReturn(searchHits);

        var page = searchService.search("xyz123abc", PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("filterByType - delegates to repository")
    void filterByType_delegatesToRepository() {
        given(claimSearchRepository.findByClaimType("AUTO")).willReturn(List.of(autoDoc));

        List<ClaimDocument> result = searchService.filterByType("AUTO");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClaimType()).isEqualTo("AUTO");
    }

    @Test
    @DisplayName("filterByStatus - delegates to repository")
    void filterByStatus_delegatesToRepository() {
        given(claimSearchRepository.findByStatus("APPROVED")).willReturn(List.of(homeDoc));

        List<ClaimDocument> result = searchService.filterByStatus("APPROVED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("filterByType - returns empty list when no matches")
    void filterByType_noMatches_returnsEmpty() {
        given(claimSearchRepository.findByClaimType("TRAVEL")).willReturn(List.of());

        List<ClaimDocument> result = searchService.filterByType("TRAVEL");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("indexClaim - saves document to repository")
    void indexClaim_savesToRepository() {
        searchService.indexClaim(autoDoc);
        verify(claimSearchRepository).save(autoDoc);
    }

    @Test
    @DisplayName("deleteClaim - removes document by id")
    void deleteClaim_removesById() {
        searchService.deleteClaim("1");
        verify(claimSearchRepository).deleteById("1");
    }
}
