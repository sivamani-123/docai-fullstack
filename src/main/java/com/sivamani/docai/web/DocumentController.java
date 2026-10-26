package com.sivamani.docai.web;

import com.sivamani.docai.dto.Dtos.DocumentDto;
import com.sivamani.docai.security.AuthUser;
import com.sivamani.docai.service.DocumentService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public List<DocumentDto> list(@AuthenticationPrincipal AuthUser user) {
        return documents.list(user.id());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDto upload(@AuthenticationPrincipal AuthUser user, @RequestParam("file") MultipartFile file) {
        return documents.upload(user.id(), file);
    }

    @PostMapping("/{id}/summarize")
    public DocumentDto summarize(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return documents.summarize(user.id(), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        documents.delete(user.id(), id);
    }
}
