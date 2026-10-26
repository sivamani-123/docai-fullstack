package com.sivamani.docai.repository;

import com.sivamani.docai.model.Chunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    List<Chunk> findByUserId(Long userId);

    List<Chunk> findByUserIdAndDocumentId(Long userId, Long documentId);

    List<Chunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Transactional
    @Modifying
    @Query("delete from Chunk c where c.documentId = :documentId")
    void removeAllForDocument(@Param("documentId") Long documentId);
}
