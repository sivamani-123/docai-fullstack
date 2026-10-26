package com.sivamani.docai.rag;

import java.util.ArrayList;
import java.util.List;

/** Splits text into overlapping chunks, preferring sentence and word boundaries. */
public final class TextChunker {

    private TextChunker() {}

    public static List<String> split(String text, int size, int overlap) {
        if (size < 100) throw new IllegalArgumentException("Chunk size must be at least 100");
        if (overlap < 0 || overlap >= size / 2) {
            throw new IllegalArgumentException("Overlap must be between 0 and half the chunk size");
        }
        String t = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        int n = t.length();
        int start = 0;
        while (start < n) {
            int end = Math.min(n, start + size);
            if (end < n) {
                int minEnd = start + size / 2;
                int dot = t.lastIndexOf(". ", end);
                if (dot >= minEnd) {
                    end = dot + 1;
                } else {
                    int space = t.lastIndexOf(' ', end);
                    if (space >= minEnd) end = space;
                }
            }
            String piece = t.substring(start, end).trim();
            if (!piece.isEmpty()) chunks.add(piece);
            if (end >= n) break;

            int next = end - overlap;
            if (next <= start) next = end;
            int space = t.indexOf(' ', next);          // start the next chunk on a word boundary
            start = (space != -1 && space < end) ? space + 1 : next;
        }
        return chunks;
    }
}
