package com.claimsplatform.aiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private Long claimId;

    @Column(nullable = false, length = 5000)
    private String content;

    private int chunkIndex;

    private String sourceFileName;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
