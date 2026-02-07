package com.gois.study.bkjavagenai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        @NotBlank String apiKey,
        String baseUrl,
        String model,
        String embeddingModel,
        String systemPrompt
) {
    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";

    public OpenAiProperties {
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (!StringUtils.hasText(model)) {
            model = DEFAULT_MODEL;
        }
        if (!StringUtils.hasText(embeddingModel)) {
            embeddingModel = DEFAULT_EMBEDDING_MODEL;
        }
        if (!StringUtils.hasText(systemPrompt)) {
            systemPrompt = DEFAULT_SYSTEM_PROMPT;
        }
    }
}
