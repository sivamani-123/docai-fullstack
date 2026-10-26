package com.sivamani.docai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents", indexes = @Index(name = "idx_documents_user", columnList = "user_id"))
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private int chunkCount;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Document() {}

    public Document(Long userId, String filename, int chunkCount) {
        this.userId = userId;
        this.filename = filename;
        this.chunkCount = chunkCount;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFilename() { return filename; }
    public int getChunkCount() { return chunkCount; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Instant getCreatedAt() { return createdAt; }
}
