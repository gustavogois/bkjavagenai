package com.gois.study.bkjavagenai.service;

import dev.langchain4j.service.UserMessage;

public interface FlightAssistant {

    String chat(@UserMessage String message);
}
