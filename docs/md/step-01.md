# Step 01 — First Contact: “Hello, LLM” with Spring Boot 4 + Java 21

[Github Repository Branch](https://github.com/gustavogois/bkjavagenai/tree/step01-hello-LLM)

This step implements a minimal, production-leaning Spring Boot application that proxies a user prompt to OpenAI and returns the model’s first answer.
The **primary goal** of this step is to demonstrate **real communication with the OpenAI API**.

## Quick Start (Real OpenAI Call)

1. Put your key in `.env`:
   `OPENAI_API_KEY=your_api_key`
2. Run the integration test:
   `./scripts/run-chat-integration-test.sh`

Expected output (example):

```
ChatController response content: Hello! I'm just a computer program, so I don't have feelings, but I'm here and ready to help you. How can I assist you today?
```
> **OpenAI API note:** This step uses the **Responses API** from the start, even though the book shows Chat Completions.

In this repository we **prioritize current best practices** and the latest platform guidance:

- **Java 21** (project baseline)
- **Spring Boot 4 / Spring Framework 7**
- **Spring HTTP Interfaces + RestClient** (clean, type-safe external API clients)
- **Responses API** for model calls
- Keep secrets out of Git: use **environment variables** for API keys
- Keep the external API stable for later refactors: the controller contract stays the same even if we swap providers or endpoints

---

## Goal

Expose a single endpoint and prove the real OpenAI API call:

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
5. Tests include a **real OpenAI integration** guarded by `OPENAI_API_KEY`.

---

## Prerequisites

- Java 21
- Spring Boot 4.x
- An OpenAI API key

---

## Configuration

### Environment variable

See `.env` and environment usage in [`scripts/run-chat-integration-test.sh`](../../scripts/run-chat-integration-test.sh).

### Application config

See [`src/main/resources/application.yml`](../../src/main/resources/application.yml).

### OpenAI config in code

See [`src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java`](../../src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java).

---

## Run

See [`README.md`](../../README.md) for run commands.

---

## Try it

See [`README.md`](../../README.md) for a curl example.

---

## Tests

The integration test is **responsible for making the real** OpenAI API call.
You can run it with the script below.
See [`README.md`](../../README.md) for general test commands.

- `ChatControllerTest` validates the endpoint contract.
- `ChatControllerIntegrationTests` makes a real OpenAI call (requires `OPENAI_API_KEY`).

Run the integration test only:

See [`scripts/run-chat-integration-test.sh`](../../scripts/run-chat-integration-test.sh).

Expected output (example):

```
ChatController response content: Hello! I'm just a computer program, so I don't have feelings, but I'm here and ready to help you. How can I assist you today?
```

The test that produces this output is in
[`src/test/java/com/gois/study/bkjavagenai/controller/ChatControllerIntegrationTests.java`](../../src/test/java/com/gois/study/bkjavagenai/controller/ChatControllerIntegrationTests.java).

---

See [`pom.xml`](../../pom.xml) for dependencies and [`src/main/java/com/gois/study/bkjavagenai`](../../src/main/java/com/gois/study/bkjavagenai) for source layout.
