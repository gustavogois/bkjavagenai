# Step 01 — First Contact: “Hello, LLM” with Spring Boot 4 + Java 21

This step implements a minimal, production-leaning Spring Boot application that proxies a user prompt to OpenAI and returns the model’s first answer.

In this repository we **prioritize current best practices** and the latest platform guidance:

- **Java 21** (project baseline)
- **Spring Boot 4 / Spring Framework 7**
- **Spring HTTP Interfaces + RestClient** (clean, type-safe external API clients)
- **Responses API** for model calls
- Keep secrets out of Git: use **environment variables** for API keys
- Keep the external API stable for later refactors: the controller contract stays the same even if we swap providers or endpoints

> **OpenAI API note:** This step uses the **Responses API** from the start, even though the book shows Chat Completions.

---

## Goal

Expose a single endpoint:

- `POST /api/chat`  
  Body: `{"userMessage":"Hello!"}`  
  Response: `{"content":"Hello! How can I assist you today?"}`

---

## Architecture

**Request flow:**

Client → `ChatController` → `ChatService` → `OpenAiResponsesClient` (HTTP Interface) → OpenAI API

**Key design decisions (vs the book):**

1. **No field injection** (`@Autowired` on fields) → constructor injection.
2. **No `RestTemplate`** → **RestClient + HTTP Interface** for cleaner, testable integration. 
3. Use **Java records** for request/response DTOs (less boilerplate).
4. Add **OpenAPI docs** early to keep contracts explicit.
5. Tests use a **real mock HTTP server** (OkHttp MockWebServer), as recommended for RestClient testing. 

---

## Prerequisites

- Java 21
- Spring Boot 4.x
- An OpenAI API key

---

## Configuration

### Environment variable

```bash
export OPENAI_API_KEY="your_api_key"
```

### Application config

`src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: genai
```

### OpenAI config in code

`src/main/java/com/gois/study/bkjavagenai/config/OpenAiConfig.java`:

```java
package com.gois.study.bkjavagenai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class OpenAiConfig {

  private static final String DEFAULT_BASE_URL = "https://api.openai.com";
  private static final String DEFAULT_MODEL = "gpt-4o-mini";
  private static final String DEFAULT_SYSTEM_PROMPT = "You are a helpful assistant.";

  @Bean
  OpenAiProperties openAiProperties(Environment environment) {
    String apiKey = environment.getProperty("OPENAI_API_KEY");
    if (!StringUtils.hasText(apiKey)) {
      apiKey = System.getenv("OPENAI_API_KEY");
    }
    if (!StringUtils.hasText(apiKey)) {
      throw new IllegalStateException("OPENAI_API_KEY is required.");
    }

    return new OpenAiProperties(
        apiKey,
        DEFAULT_BASE_URL,
        DEFAULT_MODEL,
        DEFAULT_SYSTEM_PROMPT
    );
  }
}
```

---

## Run

```bash
./mvnw spring-boot:run
```

---

## Try it

```bash
curl -X POST "http://localhost:8080/api/chat"   -H "Content-Type: application/json"   -d '{"userMessage":"Hello!"}'
```

---

---

## Tests

```bash
./mvnw test
```

- `ChatControllerTest` validates the endpoint contract.
- `ChatServiceTest` validates the OpenAI HTTP call + response parsing with a mock HTTP server.

---

# Implementation

## 1) Build dependencies (Maven)

Add (or ensure) these dependencies:

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
  </dependency>

  <!-- Mock server for HTTP client tests -->
  <dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

If you want request validation annotations like `@Valid` and `@NotBlank`, also add:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 2) File structure

```
src/main/java/com/gois/study/bkjavagenai
├── BkjavagenaiApplication.java
├── config
│   ├── OpenAiConfig.java
│   ├── OpenAiClientConfig.java
│   └── OpenAiProperties.java
├── controller
│   └── ChatController.java
├── openai
│   ├── OpenAiResponsesClient.java
│   └── dto
│       ├── ResponsesRequest.java
│       └── ResponsesResponse.java
└── service
    └── ChatService.java

src/main/resources
└── application.yml

src/test/java/com/gois/study/bkjavagenai
├── controller
│   └── ChatControllerTest.java
└── service
    └── ChatServiceTest.java
```

---

# Code

## `src/main/java/com/gois/study/bkjavagenai/BkjavagenaiApplication.java`

```java
package com.gois.study.bkjavagenai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BkjavagenaiApplication {
  public static void main(String[] args) {
    SpringApplication.run(BkjavagenaiApplication.class, args);
  }
}
```

---

## `src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java`

```java
package com.gois.study.bkjavagenai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
    @NotBlank String apiKey,
    @NotBlank String baseUrl,
    @NotBlank String model,
    @NotBlank String systemPrompt
) {}
```

---

## `src/main/java/com/gois/study/bkjavagenai/openai/dto/ResponsesRequest.java`

```java
package com.gois.study.bkjavagenai.openai.dto;

public record ResponsesRequest(String model, String input, String instructions) {}
```

---

## `src/main/java/com/gois/study/bkjavagenai/openai/dto/ResponsesResponse.java`

```java
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
```

---

## `src/main/java/com/gois/study/bkjavagenai/openai/OpenAiResponsesClient.java`

This is a **Spring HTTP Interface** representing the remote OpenAI API.

```java
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
```

---

## `src/main/java/com/gois/study/bkjavagenai/config/OpenAiClientConfig.java`

Creates a RestClient-based proxy for the HTTP Interface.

```java
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
```

---

## `src/main/java/com/gois/study/bkjavagenai/service/ChatService.java`

```java
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
```

---

## `src/main/java/com/gois/study/bkjavagenai/controller/ChatController.java`

```java
package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  public record ChatRequest(@NotBlank String userMessage) {}
  public record ChatResponse(String content) {}

  @PostMapping
  public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
    return new ChatResponse(chatService.chat(request.userMessage()));
  }
}
```

---

## `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: genai
```

---

# Tests

## `src/test/java/com/gois/study/bkjavagenai/controller/ChatControllerTest.java`

```java
package com.gois.study.bkjavagenai.controller;

import com.gois.study.bkjavagenai.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

  @Autowired MockMvc mvc;

  @MockBean ChatService chatService;

  @Test
  void shouldReturnContent() throws Exception {
    Mockito.when(chatService.chat("Hello!")).thenReturn("Hello from test!");

    mvc.perform(post("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userMessage\":\"Hello!\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").value("Hello from test!"));
  }
}
```

---

## `src/test/java/com/gois/study/bkjavagenai/service/ChatServiceTest.java`

This test runs the service against a real mock HTTP server (OkHttp MockWebServer), which is recommended for RestClient-based integrations.

```java
package com.gois.study.bkjavagenai.service;

import com.gois.study.bkjavagenai.config.OpenAiClientConfig;
import com.gois.study.bkjavagenai.config.OpenAiProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void shouldCallOpenAiAndParseFirstOutputText() throws Exception {
    String json = """
        {
          "id": "resp-test",
          "output": [
            {
              "content": [
                { "type": "output_text", "text": "Hello from mock server!" }
              ]
            }
          ]
        }
        """;

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(json));

    String baseUrl = server.url("/").toString();

    OpenAiProperties props = new OpenAiProperties(
        "test-key",
        baseUrl.substring(0, baseUrl.length() - 1), // remove trailing slash
        "gpt-4o-mini",
        "You are a helpful assistant."
    );

    // Create client beans manually for a fast unit-ish test
    RestClient restClient = RestClient.builder().baseUrl(props.baseUrl()).build();
    var cfg = new OpenAiClientConfig();
    var client = cfg.openAiResponsesClient(restClient);

    ChatService service = new ChatService(client, props);

    String answer = service.chat("Hello!");

    assertThat(answer).isEqualTo("Hello from mock server!");

    var recorded = server.takeRequest();
    assertThat(recorded.getPath()).isEqualTo("/v1/responses");
    assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test-key");

    String body = recorded.getBody().readUtf8();
    assertThat(body).contains("\"model\":\"gpt-4o-mini\"");
    assertThat(body).contains("\"input\":\"Hello!\"");
    assertThat(body).contains("\"instructions\":\"You are a helpful assistant.\"");
  }
}
```

---

## Discrepancies vs the book (and why we changed them)

- **`RestTemplate` → `RestClient + HTTP Interfaces`**: modern, fluent, and designed for today’s Spring stack.
- **Hardcoded API keys in properties**: replaced by environment variables to prevent secret leakage.
- **Returning `Map<String,String>`**: replaced by typed DTOs (`record`s) to keep contracts explicit.
- **OpenAI API choice**: we use the Responses API instead of Chat Completions.

---

## Next step

- Add richer prompts and basic prompt engineering patterns.
- Introduce error mapping (429/401) and resilience.
