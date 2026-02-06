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
class LangChainChatControllerIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(LangChainChatControllerIntegrationTests.class);

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnContentFromRealOpenAiCall() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<LangChainChatController.ChatResponse> response = restClient.post()
                .uri("/api/langchain/chat")
                .body(new LangChainChatController.ChatRequest("Say hello and ask how you can help."))
                .retrieve()
                .toEntity(LangChainChatController.ChatResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        log.info("LangChainChatController response content: {}", response.getBody().content());
        assertThat(response.getBody().content()).isNotBlank();
    }
}
