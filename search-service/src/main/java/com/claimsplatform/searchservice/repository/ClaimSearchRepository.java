package com.claimsplatform.searchservice.repository;

import com.claimsplatform.searchservice.document.ClaimDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ClaimSearchRepository extends ElasticsearchRepository<ClaimDocument, String> {

    List<ClaimDocument> findByClaimType(String claimType);

    List<ClaimDocument> findByStatus(String status);
}
