package com.gois.study.bkjavagenai.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class LangChainChatService {

    private final ChatModel chatModel;

    public LangChainChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        return chatModel.chat(message);
    }
}
