package com.gois.study.bkjavagenai.service;

import dev.langchain4j.service.MemoryId;

public interface SupportAssistant {

    String chat(@MemoryId String sessionId, String message);
}
