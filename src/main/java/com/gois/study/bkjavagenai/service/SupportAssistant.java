package com.gois.study.bkjavagenai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface SupportAssistant {

    String chat(@MemoryId String sessionId, @UserMessage String message);
}
