package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.LangChainChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/langchain/chat")
public class LangChainChatController {

    private final LangChainChatService chatService;

    public LangChainChatController(LangChainChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request.message()));
    }

    public record ChatRequest(@NotBlank String message) {}

    public record ChatResponse(String content) {}
}
