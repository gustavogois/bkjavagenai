package com.gois.study.bkjavagenai.openai;

import com.gois.study.bkjavagenai.openai.dto.ResponsesRequest;
import com.gois.study.bkjavagenai.openai.dto.ResponsesResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(url = "/v1/responses")
public interface OpenAiResponsesClient {

    @PostExchange
    ResponsesResponse createResponse(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ResponsesRequest request
    );
}