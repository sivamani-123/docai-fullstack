package com.sivamani.docai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = @Index(name = "idx_messages_session", columnList = "session_id"))
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /** USER or ASSISTANT */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    /** JSON array of the sources used for an assistant answer (null for user messages). */
    @Column(columnDefinition = "LONGTEXT")
    private String sourcesJson;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ChatMessage() {}

    public ChatMessage(Long sessionId, String role, String content, String sourcesJson) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.sourcesJson = sourcesJson;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getSourcesJson() { return sourcesJson; }
    public Instant getCreatedAt() { return createdAt; }
}
