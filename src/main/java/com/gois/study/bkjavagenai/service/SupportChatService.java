package com.gois.study.bkjavagenai.service;

import org.springframework.stereotype.Service;

@Service
public class SupportChatService {

    private final SupportAssistant assistant;

    public SupportChatService(SupportAssistant assistant) {
        this.assistant = assistant;
    }

    public String chat(String sessionId, String message) {
        return assistant.chat(sessionId, message);
    }
}
