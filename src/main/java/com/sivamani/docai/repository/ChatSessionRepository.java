package com.sivamani.docai.repository;

import com.sivamani.docai.model.ChatSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);
}
