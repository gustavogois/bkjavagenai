package com.gois.study.bkjavagenai.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Bean
    OpenAiChatModel openAiChatModel(OpenAiProperties props) {
        return OpenAiChatModel.builder()
                .apiKey(props.apiKey())
                .modelName(props.model())
                .build();
    }
}
