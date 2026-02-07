package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.model.Aircraft;
import com.gois.study.bkjavagenai.service.AircraftInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/langchain/aircraft")
public class AircraftController {

    private final AircraftInfoService aircraftInfoService;

    public AircraftController(AircraftInfoService aircraftInfoService) {
        this.aircraftInfoService = aircraftInfoService;
    }

    @PostMapping
    public AircraftResponse aircraft(@Valid @RequestBody AircraftRequest request) {
        Aircraft aircraft = aircraftInfoService.extractAircraft(request.message());
        return new AircraftResponse(aircraft.manufacturer(), aircraft.model(), aircraft.maxDistance());
    }

    public record AircraftRequest(@NotBlank String message) {}

    public record AircraftResponse(String manufacturer, String model, double maxDistance) {}
}
