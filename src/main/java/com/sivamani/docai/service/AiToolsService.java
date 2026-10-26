package com.sivamani.docai.service;

import com.sivamani.docai.ai.LlmClient;
import com.sivamani.docai.dto.Dtos.ClassifyResponse;
import com.sivamani.docai.dto.Dtos.SourceDto;
import com.sivamani.docai.rag.RetrievalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Standalone AI utilities: semantic search (embeddings) and zero-shot text classification (LLM). */
@Service
public class AiToolsService {

    private final RetrievalService retrieval;
    private final LlmClient llm;
    private final int topK;

    public AiToolsService(RetrievalService retrieval, LlmClient llm, @Value("${rag.top-k}") int topK) {
        this.retrieval = retrieval;
        this.llm = llm;
        this.topK = topK;
    }

    public List<SourceDto> search(Long userId, String query, Long documentId) {
        return retrieval.retrieve(userId, documentId, query, topK * 2);
    }

    public ClassifyResponse classify(String text, List<String> labels) {
        String prompt = "Classify the text into exactly one of these labels: " + String.join(", ", labels)
                + ".\nReply with only the label and nothing else.\n\nText:\n" + text;
        String raw = llm.chat(List.of(new LlmClient.Msg("user", prompt))).trim();
        String lower = raw.toLowerCase();
        String label = labels.stream().filter(l -> l.equalsIgnoreCase(raw)).findFirst()
                .or(() -> labels.stream().filter(l -> lower.contains(l.toLowerCase())).findFirst())
                .orElse("unknown");
        return new ClassifyResponse(label, raw);
    }
}
