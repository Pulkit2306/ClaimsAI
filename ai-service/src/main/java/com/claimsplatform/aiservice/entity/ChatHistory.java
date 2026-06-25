package com.claimsplatform.aiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false, length = 2000)
    private String userMessage;

    @Column(nullable = false, length = 5000)
    private String aiResponse;

    private Long claimId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
