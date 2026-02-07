package com.gois.study.bkjavagenai.service;

import com.gois.study.bkjavagenai.config.PromptDefaults;
import com.gois.study.bkjavagenai.model.Flight;
import com.gois.study.bkjavagenai.rag.FlightDataCatalog;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import java.util.Optional;

@Service
public class FlightInfoService {

    private final FlightAssistant flightAssistant;
    private final FlightDataCatalog flightDataCatalog;
    private final ObjectMapper objectMapper;

    public FlightInfoService(FlightAssistant flightAssistant,
                             FlightDataCatalog flightDataCatalog,
                             ObjectMapper objectMapper) {
        this.flightAssistant = flightAssistant;
        this.flightDataCatalog = flightDataCatalog;
        this.objectMapper = objectMapper;
    }

    public Optional<Flight> extractFlight(String message) {
        if (!flightDataCatalog.hasFlightNumber(message)) {
            return Optional.empty();
        }

        String response = flightAssistant.chat(message);
        if (PromptDefaults.isDefaultMessage(response)) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(response, Flight.class));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse structured flight response: " + response, ex);
        }
    }
}
