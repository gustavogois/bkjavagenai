# Step 02 — Power Tools: LangChain4j Quick-Start (Spring Boot 4 + Java 21)

## Quick Start (Real OpenAI Call)

1. Put your key in `.env`:
   `OPENAI_API_KEY=your_api_key`
2. Run the integration test:
   `./scripts/run-langchain-integration-test.sh`

Request:
```
Say hello and ask how you can help.
```

Expected output (example):

```
LangChainChatController response content: Hello! How can I assist you today?
```

---

## Objective

Demonstrate **real communication with the OpenAI API** using **LangChain4j** in a Spring Boot 4 application, exposing a simple chat endpoint and validating it via a real integration test (no mocks).

---

## Theory Summary

LangChain4j is the Java counterpart to LangChain. It provides a unified API for building LLM-powered features such as chat, prompt templating, memory, and tool use.  
In this step, we focus on the smallest viable integration: configure an OpenAI chat model, expose a REST endpoint, and validate a real API call through an integration test.

Key adaptations vs the book:

- **Spring Boot 4 + Java 21** instead of older baselines.
- **LangChain4j 1.11.0** (current library line).
- **Constructor injection** and records for request/response.
- **Real integration test** replaces curl-based verification.
- Configuration reuses the existing `openai.*` properties and environment variables.

### How we use LangChain4j 1.11.0 here

For Java/Spring developers new to LangChain4j, the mental model is simple:

- **`ChatModel` is the core abstraction**. It represents “something that can chat.”  
  In this step we use the OpenAI implementation (`OpenAiChatModel`) and expose it as a Spring bean.
- **We keep the controller thin**. The controller accepts HTTP input and delegates to a small service.  
  The service calls `chatModel.chat(message)` which triggers the real OpenAI call.
- **No framework magic is required**. There’s no annotation-based “AI service” yet—just plain Spring beans and a library-provided model.

This keeps the first integration understandable: a single model bean + a single service method + a single HTTP endpoint.

---

## Source Code (Referenced + Described)

**Configuration**

- [`src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`](../../src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java)  
  Creates the `OpenAiChatModel` bean using the existing `OpenAiProperties` (API key + model name).

- [`src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java`](../../src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java)  
  Centralized configuration for `openai.api-key`, model, base URL, and default prompt settings.

**Service + Controller**

- [`src/main/java/com/gois/study/bkjavagenai/service/LangChainChatService.java`](../../src/main/java/com/gois/study/bkjavagenai/service/LangChainChatService.java)  
  Wraps `OpenAiChatModel.generate(...)` to keep the controller thin.

- [`src/main/java/com/gois/study/bkjavagenai/controller/LangChainChatController.java`](../../src/main/java/com/gois/study/bkjavagenai/controller/LangChainChatController.java)  
  Exposes `POST /api/langchain/chat` and returns a typed JSON response.

**Integration Test (Real API Call)**

- [`src/test/java/com/gois/study/bkjavagenai/controller/LangChainChatControllerIntegrationTests.java`](../../src/test/java/com/gois/study/bkjavagenai/controller/LangChainChatControllerIntegrationTests.java)  
  Calls the endpoint with a real OpenAI request (guarded by `OPENAI_API_KEY`) and logs the response.

- [`scripts/run-langchain-integration-test.sh`](../../scripts/run-langchain-integration-test.sh)  
  Loads `.env` (if present) and runs only the integration test.

---

## Endpoint

- `POST /api/langchain/chat`  
  Body: `{"message":"Say hello"}`  
  Response: `{"content":"...model output..."}`  

---

## Notes

This step prioritizes **real API verification** over local mocks to ensure the integration works end‑to‑end.  
Future steps will expand into prompt engineering, memory, RAG, and tools while keeping the same integration‑first approach.
