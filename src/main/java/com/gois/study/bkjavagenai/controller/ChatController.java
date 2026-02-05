package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(request.userMessage()));
    }

    public record ChatRequest(@NotBlank String userMessage) {}
    public record ChatResponse(String content) {}

}