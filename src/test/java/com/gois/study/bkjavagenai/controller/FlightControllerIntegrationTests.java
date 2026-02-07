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
class FlightControllerIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(FlightControllerIntegrationTests.class);

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnStructuredFlightResponse() {
        ResponseEntity<FlightController.FlightResponse> response = postFlight("Tell me about flight details for AA4041.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().aircraft()).isNotBlank();
        assertThat(response.getBody().flightNumber()).isNotBlank();
        assertThat(response.getBody().fromCity()).isNotBlank();
        assertThat(response.getBody().toCity()).isNotBlank();

        log.info("Structured flight response: {}", response.getBody());
    }

    @Test
    void shouldReturnFlightDetailsFromRagData() {
        ResponseEntity<FlightController.FlightResponse> response = postFlight("Tell me about flight details for AA4041.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().flightNumber()).isEqualTo("AA4041");
        assertThat(response.getBody().fromCity()).isEqualTo("Denver");
        assertThat(response.getBody().toCity()).isEqualTo("Boston");
        assertThat(response.getBody().dateOfDeparture()).isEqualTo("2023-11-02");
        assertThat(response.getBody().status()).isEqualTo("On Time");
    }

    @Test
    void shouldReturnNoContentWhenUnknownFlightNumber() {
        ResponseEntity<FlightController.FlightResponse> response = postFlight("Tell me about flight details for ZZ9999.");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    private ResponseEntity<FlightController.FlightResponse> postFlight(String message) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        return restClient.post()
                .uri("/api/langchain/flight")
                .body(new FlightController.FlightRequest(message))
                .retrieve()
                .toEntity(FlightController.FlightResponse.class);
    }
}
