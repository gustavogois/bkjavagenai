package com.gois.study.bkjavagenai.service;

import tools.jackson.databind.ObjectMapper;
import com.gois.study.bkjavagenai.model.Aircraft;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AircraftInfoService {

    private final ChatModel structuredChatModel;
    private final ObjectMapper objectMapper;

    public AircraftInfoService(@Qualifier("structuredOpenAiChatModel") ChatModel structuredOpenAiChatModel,
                               ObjectMapper objectMapper) {
        this.structuredChatModel = structuredOpenAiChatModel;
        this.objectMapper = objectMapper;
    }

    public Aircraft extractAircraft(String message) {
        String json = structuredChatModel.chat(message);
        try {
            return objectMapper.readValue(json, Aircraft.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse structured response: " + json, ex);
        }
    }
}
