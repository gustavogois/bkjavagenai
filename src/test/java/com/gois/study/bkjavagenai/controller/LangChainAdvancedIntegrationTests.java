package com.gois.study.bkjavagenai.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class LangChainAdvancedIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(LangChainAdvancedIntegrationTests.class);

    @LocalServerPort
    private int port;

    private final String firstQuestion = "How is the climate in Europe in May?";
    private final String secondQuestion = "What should I pack for a trip in such a climate?";

    @Test
    void withNoContext() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<LangChainChatController.ChatResponse> first = restClient.post()
                .uri("/api/langchain/chat")
                .body(new LangChainChatController.ChatRequest(firstQuestion))
                .retrieve()
                .toEntity(LangChainChatController.ChatResponse.class);

        ResponseEntity<LangChainChatController.ChatResponse> second = restClient.post()
                .uri("/api/langchain/chat")
                .body(new LangChainChatController.ChatRequest(secondQuestion))
                .retrieve()
                .toEntity(LangChainChatController.ChatResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();

        log.info("First question: {}", firstQuestion);
        log.info("First response: {}", first.getBody().content());
        log.info("Second question: {}", secondQuestion);
        log.info("(no context) Second response: {}", second.getBody().content());

        assertThat(first.getBody().content()).isNotBlank();
        assertThat(second.getBody().content()).isNotBlank();
    }

    @Test
    void shouldUsePromptAndMemoryAcrossRequests() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String sessionId = "europe-may";

        ResponseEntity<SupportChatController.ChatResponse> first = restClient.post()
                .uri("/api/langchain/support/chat")
                .body(new SupportChatController.ChatRequest(
                        sessionId,
                        firstQuestion
                ))
                .retrieve()
                .toEntity(SupportChatController.ChatResponse.class);

        ResponseEntity<SupportChatController.ChatResponse> second = restClient.post()
                .uri("/api/langchain/support/chat")
                .body(new SupportChatController.ChatRequest(
                        sessionId,
                        secondQuestion
                ))
                .retrieve()
                .toEntity(SupportChatController.ChatResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();

        log.info("SupportChat first question: {}", firstQuestion);
        log.info("SupportChat first response: {}", first.getBody().content());
        log.info("SupportChat second question: {}", secondQuestion);
        log.info("SupportChat second response: {}", second.getBody().content());

        assertThat(first.getBody().content()).isNotBlank();
        assertThat(second.getBody().content()).isNotBlank();
    }

    @Test
    void shouldReturnStructuredAircraftResponse() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<AircraftController.AircraftResponse> response = restClient.post()
                .uri("/api/langchain/aircraft")
                .body(new AircraftController.AircraftRequest(
                        "Tell me about Boeing 777X. I need manufacturer, model, and max distance."
                ))
                .retrieve()
                .toEntity(AircraftController.AircraftResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        log.info("Aircraft response: {}", response.getBody());

        assertThat(response.getBody().manufacturer()).isNotBlank();
        assertThat(response.getBody().model()).isNotBlank();
        assertThat(response.getBody().maxDistance()).isGreaterThan(0);
    }
}
