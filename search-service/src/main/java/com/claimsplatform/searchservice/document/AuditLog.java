package com.claimsplatform.searchservice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@org.springframework.data.mongodb.core.mapping.Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    private String eventType;
    private String entityType;
    private String entityId;
    private String userId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
