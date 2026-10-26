package com.sivamani.docai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_sessions", indexes = @Index(name = "idx_sessions_user", columnList = "user_id"))
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected ChatSession() {}

    public ChatSession(Long userId, String title) {
        this.userId = userId;
        this.title = title;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
}
