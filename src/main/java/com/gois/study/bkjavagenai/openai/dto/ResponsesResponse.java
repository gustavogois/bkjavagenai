package com.gois.study.bkjavagenai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponsesResponse(
        String id,
        List<Output> output
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Output(List<Content> content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String type, String text) {}
}