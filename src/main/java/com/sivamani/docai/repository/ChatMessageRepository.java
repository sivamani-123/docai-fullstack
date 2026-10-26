package com.sivamani.docai.repository;

import com.sivamani.docai.model.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByIdAsc(Long sessionId);
    List<ChatMessage> findTop6BySessionIdOrderByIdDesc(Long sessionId);
}
