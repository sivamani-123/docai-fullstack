package com.sivamani.docai.web;

import com.sivamani.docai.dto.Dtos.AskRequest;
import com.sivamani.docai.dto.Dtos.AskResponse;
import com.sivamani.docai.dto.Dtos.MessageDto;
import com.sivamani.docai.dto.Dtos.SessionDto;
import com.sivamani.docai.security.AuthUser;
import com.sivamani.docai.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    @PostMapping("/ask")
    public AskResponse ask(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody AskRequest req) {
        return chat.ask(user.id(), req);
    }

    @GetMapping("/sessions")
    public List<SessionDto> sessions(@AuthenticationPrincipal AuthUser user) {
        return chat.sessions(user.id());
    }

    @GetMapping("/sessions/{id}/messages")
    public List<MessageDto> messages(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return chat.messages(user.id(), id);
    }
}
