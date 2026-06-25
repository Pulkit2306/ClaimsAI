package com.claimsplatform.searchservice.service;

import com.claimsplatform.searchservice.document.ClaimDocument;
import com.claimsplatform.searchservice.repository.ClaimSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ClaimSearchRepository claimSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public Page<ClaimDocument> search(String query, Pageable pageable) {
        var nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(query)
                        .fields("description^3", "aiSummary^2", "claimNumber", "claimType")
                        .fuzziness("AUTO")
                ))
                .withPageable(pageable)
                .build();

        SearchHits<ClaimDocument> hits = elasticsearchOperations.search(nativeQuery, ClaimDocument.class);

        List<ClaimDocument> content = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    public List<ClaimDocument> filterByType(String claimType) {
        return claimSearchRepository.findByClaimType(claimType);
    }

    public List<ClaimDocument> filterByStatus(String status) {
        return claimSearchRepository.findByStatus(status);
    }

    public void indexClaim(ClaimDocument document) {
        claimSearchRepository.save(document);
        log.info("Indexed claim: {}", document.getClaimNumber());
    }

    public void deleteClaim(String id) {
        claimSearchRepository.deleteById(id);
    }
}
