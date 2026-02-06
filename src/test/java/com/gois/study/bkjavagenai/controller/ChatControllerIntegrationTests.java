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
class ChatControllerIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(ChatControllerIntegrationTests.class);

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnContentFromRealOpenAiCall() {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ResponseEntity<ChatController.ChatResponse> response = restClient.post()
                .uri("/api/chat")
                .body(new ChatController.ChatRequest("Hello! How are you?"))
                .retrieve()
                .toEntity(ChatController.ChatResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        log.info("ChatController response content: {}", response.getBody().content());
        assertThat(response.getBody().content()).isNotBlank();
    }
}
