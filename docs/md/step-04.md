# Step 04 — From Chatty to Clever: Retrieval-Augmented Generation (RAG)

## Quick Start (Real OpenAI Call)

1. Put your key in `.env`:
   `OPENAI_API_KEY=your_api_key`
2. Run the integration tests:
   `./scripts/run-langchain-step04-integration-test.sh`

---

## Objective

Add **context-aware flight answers** using the RAG pattern by:

1. Defining a **Flight** domain model with a dedicated endpoint.
2. Loading internal flight data into an **embedding store** for retrieval.
3. Returning **structured JSON** responses with guardrails against hallucinations.

All validation is done through **real integration tests** (no mocks).

---

## Theory Summary

This step implements the same flow represented in the sequence diagrams:

- `docs/sequenceDiagram/rag-sequence-detailed.mmd`
- `docs/sequenceDiagram/rag-sequence-high-level.mmd`

The request does not go directly from controller to LLM anymore. It now goes through a retrieval layer that injects internal company context before generation.

### 1) Why plain LLM calls are not enough

A standard chat flow (`user query -> model answer`) works for general knowledge, but it is weak for organization-specific truth (for example, your internal flight schedule). Prompting helps, but prompting alone cannot create facts that the model was never trained on.

In Chapter 4 we explicitly keep the guardrail prompt:

```java
public static final String SYSTEM_PROMPT =
        "Please limit responses to known facts. "
                + "If you do not know the context, respond with: "
                + "\"" + DEFAULT_MESSAGE + "\"";
```

Source: `src/main/java/com/gois/study/bkjavagenai/config/PromptDefaults.java`

This reduces hallucination behavior, but the main improvement comes from retrieval.

### 2) Keyword search vs semantic vector search

Traditional search (keyword/full-text) matches literal tokens. It is fast and useful, but sensitive to wording differences. If the query wording changes, recall may drop even when intent is the same.

RAG retrieval uses semantic search:

1. Convert documents to embeddings (vectors).
2. Convert the user query to an embedding in the same vector space.
3. Retrieve nearest vectors (most semantically related chunks).

In this project, embeddings are configured with OpenAI:

```java
@Bean
EmbeddingModel openAiEmbeddingModel(OpenAiProperties props) {
    return OpenAiEmbeddingModel.builder()
            .apiKey(props.apiKey())
            .modelName(props.embeddingModel())
            .baseUrl(props.baseUrl())
            .build();
}
```

Source: `src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`

### 3) RAG pipeline in this implementation

This step follows the canonical RAG stages:

1. **Ingest** internal data (`flight-details.txt`) into a vector store.
2. **Retrieve** relevant segments for each request.
3. **Generate** structured output grounded by retrieved context.

Ingestion uses an in-memory vector store and a splitter with overlap (better retrieval quality for tabular-like text):

```java
@Bean
InMemoryEmbeddingStore<TextSegment> flightEmbeddingStore(EmbeddingModel embeddingModel,
                                                         FlightDataCatalog flightDataCatalog) {
    InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    EmbeddingStoreIngestor.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .documentSplitter(DocumentSplitters.recursive(300, 40))
            .build()
            .ingest(flightDataCatalog.documents());

    return embeddingStore;
}
```

Source: `src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`

The retriever is attached directly to the AI service:

```java
return AiServices.builder(FlightAssistant.class)
        .chatModel(flightStructuredChatModel)
        .systemMessage(PromptDefaults.SYSTEM_PROMPT)
        .contentRetriever(new EmbeddingStoreContentRetriever(flightEmbeddingStore, embeddingModel))
        .build();
```

Source: `src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`

At runtime, this means the assistant receives `query + retrieved context`, not only `query`.

### 4) Grounded generation + domain-safe output

Generation is constrained in two independent ways:

- Retrieval grounding (context comes from internal document segments).
- Structured JSON schema (response shape is validated by model configuration).

The flight model is configured with strict JSON schema:

```java
ResponseFormat responseFormat = ResponseFormat.builder()
        .type(ResponseFormatType.JSON)
        .jsonSchema(schema)
        .build();

return OpenAiChatModel.builder()
        .apiKey(props.apiKey())
        .modelName(props.model())
        .baseUrl(props.baseUrl())
        .responseFormat(responseFormat)
        .strictJsonSchema(true)
        .build();
```

Source: `src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`

Then the service parses to a typed domain record:

```java
String response = flightAssistant.chat(message);
return Optional.of(objectMapper.readValue(response, Flight.class));
```

Source: `src/main/java/com/gois/study/bkjavagenai/service/FlightInfoService.java`

### 5) Absence as first-class API behavior

This repository models absence explicitly with `Optional<Flight>` and `204 No Content`:

```java
public Optional<Flight> extractFlight(String message) {
    if (!flightDataCatalog.hasFlightNumber(message)) {
        return Optional.empty();
    }
    String response = flightAssistant.chat(message);
    if (PromptDefaults.isDefaultMessage(response)) {
        return Optional.empty();
    }
    ...
}
```

```java
return flightInfoService.extractFlight(request.message())
        .map(FlightResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
```

Sources:
- `src/main/java/com/gois/study/bkjavagenai/service/FlightInfoService.java`
- `src/main/java/com/gois/study/bkjavagenai/controller/FlightController.java`

This keeps the domain model valid (`Flight` is never “empty/null-filled”) and gives a precise HTTP contract for “not found in known context”.

---

## Source Code (Referenced + Described)

**Domain Model**

- [`src/main/java/com/gois/study/bkjavagenai/model/Flight.java`](../../src/main/java/com/gois/study/bkjavagenai/model/Flight.java)  
  Record representing a valid flight domain value.

**RAG Configuration**

- [`src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java`](../../src/main/java/com/gois/study/bkjavagenai/config/LangChain4jConfig.java)  
  Adds:
  - the OpenAI **embedding model** bean,
  - loading + ingestion of `flight-details.txt` into an in‑memory embedding store,
  - a **FlightAssistant** AI service with RAG retrieval,
  - a **structured OpenAI chat model** for JSON responses.

- [`src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java`](../../src/main/java/com/gois/study/bkjavagenai/config/OpenAiProperties.java)  
  Adds an `embeddingModel` property (default: `text-embedding-3-small`).

- [`src/main/java/com/gois/study/bkjavagenai/config/PromptDefaults.java`](../../src/main/java/com/gois/study/bkjavagenai/config/PromptDefaults.java)  
  Centralized guardrail prompt + default fallback message.

**RAG Data Catalog**

- [`src/main/java/com/gois/study/bkjavagenai/rag/FlightDataCatalog.java`](../../src/main/java/com/gois/study/bkjavagenai/rag/FlightDataCatalog.java)  
  Loads the document set and tracks known flight numbers for a deterministic fallback.

- [`src/main/resources/static/flight-details.txt`](../../src/main/resources/static/flight-details.txt)  
  Internal flight dataset ingested into the embedding store.

**Service + Controller**

- [`src/main/java/com/gois/study/bkjavagenai/service/FlightAssistant.java`](../../src/main/java/com/gois/study/bkjavagenai/service/FlightAssistant.java)  
  AI service contract for flight queries (RAG enabled).

- [`src/main/java/com/gois/study/bkjavagenai/service/FlightInfoService.java`](../../src/main/java/com/gois/study/bkjavagenai/service/FlightInfoService.java)  
  Orchestrates RAG calls, applies guardrails, and parses structured JSON into `Optional<Flight>`.

- [`src/main/java/com/gois/study/bkjavagenai/controller/FlightController.java`](../../src/main/java/com/gois/study/bkjavagenai/controller/FlightController.java)  
  Exposes `POST /api/langchain/flight` to return structured flight details.

**Integration Tests (Real API Calls)**

- [`src/test/java/com/gois/study/bkjavagenai/controller/FlightControllerIntegrationTests.java`](../../src/test/java/com/gois/study/bkjavagenai/controller/FlightControllerIntegrationTests.java)  
  Validates:
  - structured JSON shape,
  - RAG retrieval against known flight data,
  - a safe fallback for unknown flights.

- [`scripts/run-langchain-step04-integration-test.sh`](../../scripts/run-langchain-step04-integration-test.sh)  
  Loads `.env` and runs only the Step 04 integration tests.

---

## Endpoint

- `POST /api/langchain/flight`  
  Body: `{"message":"Tell me about flight details for AA4041."}`  
  Response (example):
  ```json
  {
    "aircraft": "Boeing 737",
    "dateOfDeparture": "2023-11-02",
    "fromCity": "Denver",
    "toCity": "Boston",
    "departureAirport": "DEN",
    "arrivalAirport": "BOS",
    "flightNumber": "AA4041",
    "departureTime": "13:00",
    "arrivalTime": "17:00",
    "status": "On Time"
  }
  ```
  If the flight is unknown, the API returns `204 No Content`.

---

## Notes

- This step keeps RAG intentionally lightweight: **in‑memory embedding store + local dataset**.
- Unknown flight numbers return **204 No Content** instead of hallucinated data.
- The next step will move toward **Spring AI** for deeper integrations.
