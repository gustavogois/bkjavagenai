package com.gois.study.bkjavagenai.config;

import com.gois.study.bkjavagenai.openai.OpenAiResponsesClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfig {

    @Bean
    RestClient openAiRestClient(OpenAiProperties props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }

    @Bean
    OpenAiResponsesClient openAiResponsesClient(RestClient openAiRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(openAiRestClient))
                .build();
        return factory.createClient(OpenAiResponsesClient.class);
    }
}