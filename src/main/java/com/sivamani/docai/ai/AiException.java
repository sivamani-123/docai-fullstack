package com.sivamani.docai.ai;

/** Raised when the LLM / embedding provider cannot be reached or returns an unusable answer. */
public class AiException extends RuntimeException {
    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
