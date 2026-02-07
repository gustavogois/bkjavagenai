package com.gois.study.bkjavagenai.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record Flight(
        String aircraft,
        String dateOfDeparture,
        String fromCity,
        String toCity,
        String departureAirport,
        String arrivalAirport,
        String flightNumber,
        String departureTime,
        String arrivalTime,
        String status
) {

    public static final int LIMIT_EMPTY_COLUMNS = -1;
    public static final int FLIGHT_NUMBER_INDEX = 6;
    public static final int FLIGHT_NUMBER_COLUMN = 7;

    /**
     * Extracts flight numbers from a CSV-like text block.
     *
     * <p>The input is expected to contain multiple lines, where each line represents
     * a record and fields are separated by commas. The flight number is assumed to be
     * located at index 6 (7th column).</p>
     *
     * <p>Empty lines, blank input, and header lines (starting with "Aircraft,") are ignored.
     * The returned set preserves insertion order and guarantees uniqueness.</p>
     *
     * @param text raw text containing flight data (may be {@code null} or blank)
     * @return an ordered set of extracted flight numbers, never {@code null}
     */
    public static Set<String> parseFlightNumbers(String text) {
        // LinkedHashSet preserves insertion order while avoiding duplicates
        Set<String> flightNumbers = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return flightNumbers;
        }

        // Split input by any line separator (\n, \r\n, etc.)
        String[] lines = text.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and CSV header rows
            if (trimmed.isEmpty() || trimmed.startsWith("Aircraft,")) {
                continue;
            }
            // Split CSV line preserving empty columns (-1)
            String[] parts = trimmed.split(",", LIMIT_EMPTY_COLUMNS);
            // Flight number is expected at index 6 (7th column)
            if (parts.length >= FLIGHT_NUMBER_COLUMN) {
                flightNumbers.add(parts[FLIGHT_NUMBER_INDEX].trim());
            }
        }
        return flightNumbers;
    }
}
