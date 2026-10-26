package com.sivamani.docai.rag;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    @Test
    void shortTextIsOneChunk() {
        List<String> chunks = TextChunker.split("Hello world. This is short.", 200, 40);
        assertEquals(1, chunks.size());
    }

    @Test
    void longTextIsSplitWithinSizeAndCoversTheEnd() {
        String sentence = "Retrieval augmented generation grounds answers in documents. ";
        String text = sentence.repeat(60);
        List<String> chunks = TextChunker.split(text, 300, 60);
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().allMatch(c -> c.length() <= 300));
        assertTrue(chunks.get(chunks.size() - 1).endsWith("documents."));
    }

    @Test
    void emptyTextGivesNoChunks() {
        assertTrue(TextChunker.split("   \n ", 200, 40).isEmpty());
    }

    @Test
    void invalidOverlapIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> TextChunker.split("text", 200, 150));
    }
}
