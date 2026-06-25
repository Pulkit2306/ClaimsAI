package com.claimsplatform.searchservice.repository;

import com.claimsplatform.searchservice.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);
}
