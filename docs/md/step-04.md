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

RAG (Retrieval‑Augmented Generation) improves accuracy by injecting **organization‑specific context** into each LLM request. Instead of relying on the model’s general knowledge, we:

- **Embed internal documents** into vectors (semantic representations).
- Store them in a **vector store** for similarity search.
- **Retrieve only relevant chunks** and provide them to the LLM at query time.

This reduces hallucinations and keeps answers aligned with proprietary data. In this step, we keep it intentionally lightweight:

- A **CSV‑style dataset** of flights is stored in `resources`.
- LangChain4j ingests the file into an **in‑memory embedding store**.
- A dedicated AI service retrieves context and outputs **structured JSON** for `Flight`.

Key adaptations vs the book:

- **Spring Boot 4 + Java 21** conventions.
- **LangChain4j 1.11.0** with modern RAG APIs.
- **Embedding model configuration** via `OpenAiProperties`.
- **Integration tests** instead of curl.

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
