package com.gois.study.bkjavagenai.config;

public record OpenAiProperties(
        String apiKey,
        String baseUrl,
        String model,
        String systemPrompt
) {
}
