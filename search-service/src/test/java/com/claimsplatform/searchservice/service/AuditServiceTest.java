package com.claimsplatform.searchservice.service;

import com.claimsplatform.searchservice.document.AuditLog;
import com.claimsplatform.searchservice.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("log - persists AuditLog with correct fields")
    void log_persistsCorrectFields() {
        auditService.log("CLAIM_CREATED", "CLAIM", "42", "adjuster@intact.ca",
                "Claim submitted", "AUTO claim for $8,500");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo("CLAIM_CREATED");
        assertThat(saved.getEntityType()).isEqualTo("CLAIM");
        assertThat(saved.getEntityId()).isEqualTo("42");
        assertThat(saved.getUserId()).isEqualTo("adjuster@intact.ca");
        assertThat(saved.getAction()).isEqualTo("Claim submitted");
        assertThat(saved.getDetails()).isEqualTo("AUTO claim for $8,500");
        assertThat(saved.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("getEntityHistory - delegates to repository with correct args")
    void getEntityHistory_delegatesToRepository() {
        AuditLog log = AuditLog.builder()
                .id("a1").entityType("CLAIM").entityId("42")
                .timestamp(LocalDateTime.now()).build();

        given(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("CLAIM", "42"))
                .willReturn(List.of(log));

        List<AuditLog> result = auditService.getEntityHistory("CLAIM", "42");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo("42");
    }

    @Test
    @DisplayName("getUserActivity - returns logs ordered by most recent")
    void getUserActivity_delegatesToRepository() {
        AuditLog log1 = AuditLog.builder().id("a1").userId("user@test.com")
                .eventType("CLAIM_CREATED").timestamp(LocalDateTime.now()).build();
        AuditLog log2 = AuditLog.builder().id("a2").userId("user@test.com")
                .eventType("CLAIM_STATUS_CHANGED").timestamp(LocalDateTime.now().minusHours(1)).build();

        given(auditLogRepository.findByUserIdOrderByTimestampDesc("user@test.com"))
                .willReturn(List.of(log1, log2));

        List<AuditLog> result = auditService.getUserActivity("user@test.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEventType()).isEqualTo("CLAIM_CREATED");
    }

    @Test
    @DisplayName("getEntityHistory - returns empty list when no history")
    void getEntityHistory_noHistory_returnsEmpty() {
        given(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("CLAIM", "999"))
                .willReturn(List.of());

        List<AuditLog> result = auditService.getEntityHistory("CLAIM", "999");

        assertThat(result).isEmpty();
    }
}
