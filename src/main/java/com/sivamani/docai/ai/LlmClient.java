package com.sivamani.docai.ai;

import java.util.List;

/** Provider-independent access to a chat LLM and an embedding model. */
public interface LlmClient {

    /** role is "system", "user" or "assistant". */
    record Msg(String role, String content) {}

    String chat(List<Msg> messages);

    /** Returns one embedding vector per input text, in the same order. */
    List<float[]> embed(List<String> texts);
}
