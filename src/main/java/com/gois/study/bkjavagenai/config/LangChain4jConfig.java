package com.gois.study.bkjavagenai.config;

import com.gois.study.bkjavagenai.memory.MapDbChatMemoryStore;
import com.gois.study.bkjavagenai.service.SupportAssistant;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

@Configuration
public class LangChain4jConfig {

    @Bean
    @Primary
    OpenAiChatModel openAiChatModel(OpenAiProperties props) {
        return OpenAiChatModel.builder()
                .apiKey(props.apiKey())
                .modelName(props.model())
                .build();
    }

    @Bean(name = "structuredOpenAiChatModel")
    OpenAiChatModel structuredOpenAiChatModel(OpenAiProperties props) {
        JsonSchema schema = JsonSchema.builder()
                .name("Aircraft")
                .rootElement(JsonObjectSchema.builder()
                        .addStringProperty("manufacturer", "Aircraft manufacturer")
                        .addStringProperty("model", "Aircraft model")
                        .addNumberProperty("maxDistance", "Maximum distance (nautical miles)")
                        .required("manufacturer", "model", "maxDistance")
                        .additionalProperties(false)
                        .build())
                .build();

        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(schema)
                .build();

        return OpenAiChatModel.builder()
                .apiKey(props.apiKey())
                .modelName(props.model())
                .responseFormat(responseFormat)
                .strictJsonSchema(true)
                .build();
    }

    @Bean
    ChatMemoryStore chatMemoryStore() {
        return new MapDbChatMemoryStore("chat-memory.db");
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();
    }

    @Bean
    SupportAssistant supportAssistant(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel,
                                      ChatMemoryProvider chatMemoryProvider) {
        String systemPrompt = "Please limit responses to known facts. "
                + "If you do not know the context, respond with: "
                + "\"Sorry, I am not aware of this context. Please let me know if I can help you with any other requests.\"";

        return AiServices.builder(SupportAssistant.class)
                .chatModel(openAiChatModel)
                .systemMessage(systemPrompt)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
