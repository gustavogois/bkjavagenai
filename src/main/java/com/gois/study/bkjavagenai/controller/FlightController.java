package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.model.Flight;
import com.gois.study.bkjavagenai.service.FlightInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/langchain/flight")
public class FlightController {

    private final FlightInfoService flightInfoService;

    public FlightController(FlightInfoService flightInfoService) {
        this.flightInfoService = flightInfoService;
    }

    @PostMapping
    public ResponseEntity<FlightResponse> flight(@Valid @RequestBody FlightRequest request) {
        return flightInfoService.extractFlight(request.message())
                .map(FlightResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    public record FlightRequest(@NotBlank String message) {}

    public record FlightResponse(
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
        public static FlightResponse from(Flight f) {
            return new FlightResponse(
                    f.aircraft(), f.dateOfDeparture(), f.fromCity(), f.toCity(),
                    f.departureAirport(), f.arrivalAirport(), f.flightNumber(),
                    f.departureTime(), f.arrivalTime(), f.status()
            );
    }}
}
