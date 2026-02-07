package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.SupportChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/langchain/support/chat")
public class SupportChatController {

    private final SupportChatService chatService;

    public SupportChatController(SupportChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request.sessionId(), request.message()));
    }

    public record ChatRequest(@NotBlank String sessionId, @NotBlank String message) {}

    public record ChatResponse(String content) {}
}
