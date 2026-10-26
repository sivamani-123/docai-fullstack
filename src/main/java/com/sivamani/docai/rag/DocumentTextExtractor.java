package com.sivamani.docai.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Extracts text per page from PDF files, or the whole text from TXT / MD files. */
@Component
public class DocumentTextExtractor {

    /** page is null for plain-text files. */
    public record PageText(Integer page, String text) {}

    public List<PageText> extract(String filename, byte[] data) throws IOException {
        String name = filename.toLowerCase();
        List<PageText> pages = new ArrayList<>();
        if (name.endsWith(".pdf")) {
            try (PDDocument pdf = Loader.loadPDF(data)) {
                PDFTextStripper stripper = new PDFTextStripper();
                for (int p = 1; p <= pdf.getNumberOfPages(); p++) {
                    stripper.setStartPage(p);
                    stripper.setEndPage(p);
                    String text = stripper.getText(pdf);
                    if (!text.isBlank()) pages.add(new PageText(p, text));
                }
            }
        } else if (name.endsWith(".txt") || name.endsWith(".md")) {
            String text = new String(data, StandardCharsets.UTF_8);
            if (!text.isBlank()) pages.add(new PageText(null, text));
        } else {
            throw new IllegalArgumentException("Only PDF, TXT and MD files are supported");
        }
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("No readable text found (scanned PDFs need OCR first)");
        }
        return pages;
    }
}
