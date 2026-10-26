package com.sivamani.docai.service;

import com.sivamani.docai.ai.LlmClient;
import com.sivamani.docai.dto.Dtos.DocumentDto;
import com.sivamani.docai.model.Chunk;
import com.sivamani.docai.model.Document;
import com.sivamani.docai.rag.DocumentTextExtractor;
import com.sivamani.docai.rag.DocumentTextExtractor.PageText;
import com.sivamani.docai.rag.TextChunker;
import com.sivamani.docai.rag.VectorMath;
import com.sivamani.docai.repository.ChunkRepository;
import com.sivamani.docai.repository.DocumentRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentService {

    private static final int SUMMARY_INPUT_LIMIT = 8000;

    private final DocumentRepository documents;
    private final ChunkRepository chunks;
    private final DocumentTextExtractor extractor;
    private final LlmClient llm;
    private final int chunkSize;
    private final int chunkOverlap;
    private final int batchSize;

    public DocumentService(DocumentRepository documents, ChunkRepository chunks,
                           DocumentTextExtractor extractor, LlmClient llm,
                           @Value("${rag.chunk-size}") int chunkSize,
                           @Value("${rag.chunk-overlap}") int chunkOverlap,
                           @Value("${rag.embed-batch-size}") int batchSize) {
        this.documents = documents;
        this.chunks = chunks;
        this.extractor = extractor;
        this.llm = llm;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.batchSize = batchSize;
    }

    public List<DocumentDto> list(Long userId) {
        return documents.findByUserIdOrderByCreatedAtDesc(userId).stream().map(DocumentService::toDto).toList();
    }

    /** Pipeline: extract text -> chunk -> embed (batched) -> store document and vectors. */
    public DocumentDto upload(Long userId, MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("The file is empty");
        String filename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "document"));

        List<PageText> pages;
        try {
            pages = extractor.extract(filename, file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file: " + e.getMessage());
        }

        record Piece(Integer page, String text) {}
        List<Piece> pieces = new ArrayList<>();
        for (PageText page : pages) {
            for (String text : TextChunker.split(page.text(), chunkSize, chunkOverlap)) {
                pieces.add(new Piece(page.page(), text));
            }
        }
        if (pieces.isEmpty()) throw new IllegalArgumentException("No readable text found in the file");

        List<float[]> vectors = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i += batchSize) {
            List<String> batch = pieces.subList(i, Math.min(pieces.size(), i + batchSize))
                    .stream().map(Piece::text).toList();
            vectors.addAll(llm.embed(batch));
        }

        Document doc = documents.save(new Document(userId, filename, pieces.size()));
        try {
            List<Chunk> rows = new ArrayList<>();
            for (int i = 0; i < pieces.size(); i++) {
                rows.add(new Chunk(userId, doc.getId(), i, pieces.get(i).page(),
                        pieces.get(i).text(), VectorMath.toBytes(vectors.get(i))));
            }
            chunks.saveAll(rows);
        } catch (RuntimeException e) {
            documents.deleteById(doc.getId());
            throw e;
        }
        return toDto(doc);
    }

    public DocumentDto summarize(Long userId, Long documentId) {
        Document doc = find(userId, documentId);
        String text = chunks.findByDocumentIdOrderByChunkIndexAsc(documentId).stream()
                .map(Chunk::getContent).collect(Collectors.joining(" "));
        if (text.length() > SUMMARY_INPUT_LIMIT) text = text.substring(0, SUMMARY_INPUT_LIMIT);
        String answer = llm.chat(List.of(
                new LlmClient.Msg("system", "You summarise documents accurately and concisely."),
                new LlmClient.Msg("user", "Summarise the following document in 5 short bullet points.\n\n" + text)));
        doc.setSummary(answer);
        return toDto(documents.save(doc));
    }

    public void delete(Long userId, Long documentId) {
        Document doc = find(userId, documentId);
        chunks.removeAllForDocument(doc.getId());
        documents.delete(doc);
    }

    private Document find(Long userId, Long documentId) {
        return documents.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    private static DocumentDto toDto(Document d) {
        return new DocumentDto(d.getId(), d.getFilename(), d.getChunkCount(), d.getSummary(), d.getCreatedAt());
    }
}
