package com.sivamani.docai.rag;

import com.sivamani.docai.ai.LlmClient;
import com.sivamani.docai.dto.Dtos.SourceDto;
import com.sivamani.docai.model.Chunk;
import com.sivamani.docai.model.Document;
import com.sivamani.docai.repository.ChunkRepository;
import com.sivamani.docai.repository.DocumentRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Semantic retrieval: embeds the query and ranks the user's chunks by cosine similarity.
 * Vectors live in MySQL and are scored in memory, which is fine for thousands of chunks.
 * For larger corpora swap this class for a vector database (pgvector, Qdrant, Milvus).
 */
@Service
public class RetrievalService {

    private final LlmClient llm;
    private final ChunkRepository chunks;
    private final DocumentRepository documents;

    public RetrievalService(LlmClient llm, ChunkRepository chunks, DocumentRepository documents) {
        this.llm = llm;
        this.chunks = chunks;
        this.documents = documents;
    }

    public List<SourceDto> retrieve(Long userId, Long documentId, String query, int topK) {
        List<Chunk> candidates = documentId == null
                ? chunks.findByUserId(userId)
                : chunks.findByUserIdAndDocumentId(userId, documentId);
        if (candidates.isEmpty()) return List.of();

        float[] queryVector = llm.embed(List.of(query)).get(0);
        Map<Long, String> names = documents.findAllById(
                        candidates.stream().map(Chunk::getDocumentId).distinct().toList())
                .stream().collect(Collectors.toMap(Document::getId, Document::getFilename));

        record Scored(Chunk chunk, double score) {}
        return candidates.stream()
                .map(c -> new Scored(c, VectorMath.cosine(queryVector, VectorMath.fromBytes(c.getEmbedding()))))
                .filter(s -> s.score() > -1)
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(topK)
                .map(s -> new SourceDto(s.chunk().getDocumentId(),
                        names.getOrDefault(s.chunk().getDocumentId(), "unknown"),
                        s.chunk().getPageNumber(), s.chunk().getChunkIndex(),
                        Math.round(s.score() * 10000.0) / 10000.0, s.chunk().getContent()))
                .toList();
    }
}
