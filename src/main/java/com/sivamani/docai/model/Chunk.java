package com.sivamani.docai.model;

import jakarta.persistence.*;

/** One text chunk of a document together with its embedding vector (float32, packed). */
@Entity
@Table(name = "chunks", indexes = {
        @Index(name = "idx_chunks_user", columnList = "user_id"),
        @Index(name = "idx_chunks_document", columnList = "document_id")
})
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private int chunkIndex;

    private Integer pageNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] embedding;

    protected Chunk() {}

    public Chunk(Long userId, Long documentId, int chunkIndex, Integer pageNumber,
                 String content, byte[] embedding) {
        this.userId = userId;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.pageNumber = pageNumber;
        this.content = content;
        this.embedding = embedding;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getDocumentId() { return documentId; }
    public int getChunkIndex() { return chunkIndex; }
    public Integer getPageNumber() { return pageNumber; }
    public String getContent() { return content; }
    public byte[] getEmbedding() { return embedding; }
}
