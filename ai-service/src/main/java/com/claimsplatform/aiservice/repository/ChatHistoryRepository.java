package com.claimsplatform.aiservice.repository;

import com.claimsplatform.aiservice.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    List<ChatHistory> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
