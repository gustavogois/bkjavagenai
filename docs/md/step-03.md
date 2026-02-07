# Step 03 — Integrating LLMs with Java Applications (LangChain4j 1.11)

## Quick Start (Real OpenAI Call)

1. Put your key in `.env`:
   `OPENAI_API_KEY=your_api_key`
2. Run the integration tests:
   `./scripts/run-langchain-step03-integration-test.sh`

Expected output (example):

```
SupportChat first response: ...
SupportChat second response: ...
Aircraft response: ...
```

---

## Objective

Show a **deeper LangChain4j integration** by adding:

- prompt engineering to reduce hallucinations,
- persistent chat memory for multi‑turn conversations,
- structured outputs for reliable JSON responses.

All validation is done through **real integration tests** (no mocks).

---

## Theory Summary

This chapter moves beyond “single prompt → single response” and covers three practical capabilities:

1. **Prompt engineering**  
   We add a system message that constrains the model to known facts and a safe fallback response.

2. **Chat memory**  
   Follow‑up questions need context. LangChain4j provides memory abstractions that keep the last N messages.  
   We persist the memory using MapDB so it survives process restarts.

3. **Structured outputs**  
   For downstream systems, JSON is more reliable than free‑text.  
   We configure OpenAI’s response format with a JSON schema and parse the result into a typed Java record.

All of this is implemented using **LangChain4j 1.11** with Spring Boot 4 + Java 21.

---

## Source Code (Referenced + Described)

**Configuration**

- [`src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`](../../src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java)  
  Builds:
  - the standard `OpenAiChatModel`,
  - a **structured** `OpenAiChatModel` with JSON schema,
  - the `SupportAssistant` AI service with memory + system prompt.

- [`src/main/java/com/gois/study/bkjavagenai/memory/MapDbChatMemoryStore.java`](../../src/main/java/com/gois/study/bkjavagenai/memory/MapDbChatMemoryStore.java)  
  Persists chat memory using MapDB.

**Prompt Engineering + Memory**

- [`src/main/java/com/gois/study/bkjavagenai/service/SupportAssistant.java`](../../src/main/java/com/gois/study/bkjavagenai/service/SupportAssistant.java)  
  AI service contract using `@MemoryId` for session-based memory.

- [`src/main/java/com/gois/study/bkjavagenai/service/SupportChatService.java`](../../src/main/java/com/gois/study/bkjavagenai/service/SupportChatService.java)  
  Thin service that delegates to the AI assistant.

- [`src/main/java/com/gois/study/bkjavagenai/controller/SupportChatController.java`](../../src/main/java/com/gois/study/bkjavagenai/controller/SupportChatController.java)  
  `POST /api/langchain/support/chat` with `sessionId` + `message`.

**Structured Output**

- [`src/main/java/com/gois/study/bkjavagenai/model/Aircraft.java`](../../src/main/java/com/gois/study/bkjavagenai/model/Aircraft.java)  
  Target schema for structured output.

- [`src/main/java/com/gois/study/bkjavagenai/service/AircraftInfoService.java`](../../src/main/java/com/gois/study/bkjavagenai/service/AircraftInfoService.java)  
  Calls the structured model and maps JSON into `Aircraft`.

- [`src/main/java/com/gois/study/bkjavagenai/controller/AircraftController.java`](../../src/main/java/com/gois/study/bkjavagenai/controller/AircraftController.java)  
  `POST /api/langchain/aircraft` returns structured JSON.

**Integration Tests (Real API Calls)**

- [`src/test/java/com/gois/study/bkjavagenai/controller/LangChainAdvancedIntegrationTests.java`](../../src/test/java/com/gois/study/bkjavagenai/controller/LangChainAdvancedIntegrationTests.java)  
  Verifies memory‑aware chat + structured aircraft output against the real API.

- [`scripts/run-langchain-step03-integration-test.sh`](../../scripts/run-langchain-step03-integration-test.sh)  
  Loads `.env` and runs only the Step 03 integration test.

---

## Endpoints

- `POST /api/langchain/support/chat`  
  Body: `{"sessionId":"europe-may","message":"How is the climate in Europe in May?"}`  
  Response: `{"content":"..."}`

- `POST /api/langchain/aircraft`  
  Body: `{"message":"Tell me about Boeing 777X..."}`  
  Response: `{"manufacturer":"...","model":"...","maxDistance":1234.5}`

---

## Notes

This step keeps the focus on **production‑grade integration patterns**:

- prompt engineering as a guardrail,
- persistent memory for multi‑turn coherence,
- structured outputs for downstream automation.
