package com.gois.study.bkjavagenai.rag;

import dev.langchain4j.data.document.Document;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public record FlightDataCatalog(List<Document> documents, Set<String> flightNumbers) {

    private static final Pattern FLIGHT_NUMBER_PATTERN = Pattern.compile("[A-Z]{2}\\d{3,4}");

    public boolean hasFlightNumber(String message) {
        return extractFlightNumber(message)
                .map(flightNumbers::contains)
                .orElse(false);
    }

    public Optional<String> extractFlightNumber(String message) {
        if (message == null) {
            return Optional.empty();
        }
        var matcher = FLIGHT_NUMBER_PATTERN.matcher(message.toUpperCase(Locale.ROOT));
        return matcher.find() ? Optional.of(matcher.group()) : Optional.empty();
    }
}
