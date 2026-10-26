package com.sivamani.docai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/** Request and response objects of the REST API. */
public final class Dtos {

    private Dtos() {}

    public record RegisterRequest(@NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 100, message = "Password must be 8 to 100 characters") String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, String email) {}

    public record DocumentDto(Long id, String filename, int chunkCount, String summary, Instant createdAt) {}

    /** mode: "RAG" (answer from documents, default) or "LLM" (model only). */
    public record AskRequest(@NotBlank @Size(max = 2000) String question, Long sessionId, String mode, Long documentId) {}

    public record SourceDto(Long documentId, String filename, Integer page, int chunkIndex, double score, String text) {}

    public record AskResponse(Long sessionId, String answer, List<SourceDto> sources) {}

    public record SessionDto(Long id, String title, Instant createdAt) {}

    public record MessageDto(String role, String content, List<SourceDto> sources, Instant createdAt) {}

    public record SearchRequest(@NotBlank String query, Long documentId) {}

    public record ClassifyRequest(@NotBlank @Size(max = 5000) String text,
                                  @NotEmpty @Size(max = 20) List<@NotBlank String> labels) {}

    public record ClassifyResponse(String label, String raw) {}
}
