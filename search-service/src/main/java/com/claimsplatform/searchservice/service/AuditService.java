package com.claimsplatform.searchservice.service;

import com.claimsplatform.searchservice.document.AuditLog;
import com.claimsplatform.searchservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String eventType, String entityType, String entityId, String userId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getEntityHistory(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public List<AuditLog> getUserActivity(String userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
