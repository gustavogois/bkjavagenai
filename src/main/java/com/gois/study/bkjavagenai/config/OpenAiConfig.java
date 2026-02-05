package com.gois.study.bkjavagenai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class OpenAiConfig {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";

    @Bean
    OpenAiProperties openAiProperties(Environment environment) {
        String apiKey = environment.getProperty("OPENAI_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OPENAI_API_KEY is required.");
        }

        return new OpenAiProperties(
                apiKey,
                DEFAULT_BASE_URL,
                DEFAULT_MODEL,
                DEFAULT_SYSTEM_PROMPT
        );
    }
}
