package com.claimsplatform.aiservice.repository;

import com.claimsplatform.aiservice.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByClaimId(Long claimId);
    List<DocumentChunk> findByDocumentId(String documentId);
}
