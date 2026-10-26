package com.sivamani.docai.repository;

import com.sivamani.docai.model.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}
