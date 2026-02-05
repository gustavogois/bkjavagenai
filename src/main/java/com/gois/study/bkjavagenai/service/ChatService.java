package com.gois.study.bkjavagenai.service;

import com.gois.study.bkjavagenai.config.OpenAiProperties;
import com.gois.study.bkjavagenai.openai.OpenAiResponsesClient;
import com.gois.study.bkjavagenai.openai.dto.ResponsesRequest;
import com.gois.study.bkjavagenai.openai.dto.ResponsesResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OpenAiResponsesClient client;
    private final OpenAiProperties props;

    public ChatService(OpenAiResponsesClient client, OpenAiProperties props) {
        this.client = client;
        this.props = props;
    }

    public String chat(String userMessage) {
        var req = new ResponsesRequest(
                props.model(),
                userMessage,
                props.systemPrompt()
        );

        ResponsesResponse resp = client.createResponse(
                "Bearer " + props.apiKey(),
                req
        );

        if (resp == null || resp.output() == null || resp.output().isEmpty()) {
            throw new IllegalStateException("OpenAI returned an empty response.");
        }

        for (var out : resp.output()) {
            if (out.content() == null) {
                continue;
            }
            for (var content : out.content()) {
                if ("output_text".equals(content.type())) {
                    return content.text() != null ? content.text() : "";
                }
            }
        }

        return "";
    }
}