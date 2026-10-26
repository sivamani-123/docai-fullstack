package com.sivamani.docai.web;

import com.sivamani.docai.dto.Dtos.ClassifyRequest;
import com.sivamani.docai.dto.Dtos.ClassifyResponse;
import com.sivamani.docai.dto.Dtos.SearchRequest;
import com.sivamani.docai.dto.Dtos.SourceDto;
import com.sivamani.docai.security.AuthUser;
import com.sivamani.docai.service.AiToolsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiToolsController {

    private final AiToolsService tools;

    public AiToolsController(AiToolsService tools) {
        this.tools = tools;
    }

    @PostMapping("/search")
    public List<SourceDto> search(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody SearchRequest req) {
        return tools.search(user.id(), req.query(), req.documentId());
    }

    @PostMapping("/classify")
    public ClassifyResponse classify(@Valid @RequestBody ClassifyRequest req) {
        return tools.classify(req.text(), req.labels());
    }
}
