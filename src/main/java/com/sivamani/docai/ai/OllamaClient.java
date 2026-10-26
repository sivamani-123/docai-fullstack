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

/** Local, free models served by Ollama (https://ollama.com). Default provider. */
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaClient implements LlmClient {

    private final RestClient http;
    private final String baseUrl;
    private final String chatModel;
    private final String embedModel;

    public OllamaClient(@Value("${ai.ollama.base-url}") String baseUrl,
                        @Value("${ai.ollama.chat-model}") String chatModel,
                        @Value("${ai.ollama.embed-model}") String embedModel) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(300_000);  // local models can be slow on CPU
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
    }

    @Override
    public String chat(List<Msg> messages) {
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "stream", false,
                "options", Map.of("temperature", 0.2),
                "messages", messages.stream()
                        .map(m -> Map.of("role", m.role(), "content", m.content())).toList());
        try {
            JsonNode res = http.post().uri("/api/chat").contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            String answer = res == null ? "" : res.path("message").path("content").asText("");
            if (answer.isBlank()) throw new AiException("The model returned an empty answer.");
            return answer.trim();
        } catch (RestClientException e) {
            throw new AiException("Cannot reach Ollama at " + baseUrl + " or model '" + chatModel
                    + "' is missing. Start Ollama and run: ollama pull " + chatModel, e);
        }
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        Map<String, Object> body = Map.of("model", embedModel, "input", texts);
        try {
            JsonNode res = http.post().uri("/api/embed").contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            JsonNode arr = res == null ? null : res.path("embeddings");
            if (arr == null || !arr.isArray() || arr.size() != texts.size()) {
                throw new AiException("Unexpected embedding response from Ollama.");
            }
            List<float[]> out = new ArrayList<>();
            for (JsonNode vec : arr) {
                float[] v = new float[vec.size()];
                for (int i = 0; i < v.length; i++) v[i] = (float) vec.get(i).asDouble();
                out.add(v);
            }
            return out;
        } catch (RestClientException e) {
            throw new AiException("Cannot reach Ollama at " + baseUrl + " or model '" + embedModel
                    + "' is missing. Start Ollama and run: ollama pull " + embedModel, e);
        }
    }
}
