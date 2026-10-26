package com.sivamani.docai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** OpenAI-compatible hosted models. Enable with ai.provider=openai and OPENAI_API_KEY. */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiClient implements LlmClient {

    private final RestClient http;
    private final String apiKey;
    private final String chatModel;
    private final String embedModel;

    public OpenAiClient(@Value("${ai.openai.base-url}") String baseUrl,
                        @Value("${ai.openai.api-key}") String apiKey,
                        @Value("${ai.openai.chat-model}") String chatModel,
                        @Value("${ai.openai.embed-model}") String embedModel) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.apiKey = apiKey;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
    }

    private void requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiException("OPENAI_API_KEY is not set.");
        }
    }

    @Override
    public String chat(List<Msg> messages) {
        requireKey();
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "temperature", 0.2,
                "messages", messages.stream()
                        .map(m -> Map.of("role", m.role(), "content", m.content())).toList());
        try {
            JsonNode res = http.post().uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            String answer = res == null ? "" : res.path("choices").path(0).path("message").path("content").asText("");
            if (answer.isBlank()) throw new AiException("The model returned an empty answer.");
            return answer.trim();
        } catch (RestClientException e) {
            throw new AiException("OpenAI request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        requireKey();
        Map<String, Object> body = Map.of("model", embedModel, "input", texts);
        try {
            JsonNode res = http.post().uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            JsonNode data = res == null ? null : res.path("data");
            if (data == null || !data.isArray() || data.size() != texts.size()) {
                throw new AiException("Unexpected embedding response from OpenAI.");
            }
            List<float[]> out = new ArrayList<>();
            for (JsonNode item : data) {
                JsonNode vec = item.path("embedding");
                float[] v = new float[vec.size()];
                for (int i = 0; i < v.length; i++) v[i] = (float) vec.get(i).asDouble();
                out.add(v);
            }
            return out;
        } catch (RestClientException e) {
            throw new AiException("OpenAI embedding request failed: " + e.getMessage(), e);
        }
    }
}
