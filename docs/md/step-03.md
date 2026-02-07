# Step 03 — Integrating LLMs with Java Applications (LangChain4j 1.11)

## Quick Start (Real OpenAI Call)

1. Put your key in `.env`:
   `OPENAI_API_KEY=your_api_key`
2. Run the integration tests:
   `./scripts/run-langchain-step03-integration-test.sh`

---

## Objective

Show a **deeper LangChain4j integration** by adding:

1) prompt engineering to reduce hallucinations,
2) persistent chat memory for multi‑turn conversations,
3) structured outputs for reliable JSON responses.

All validation is done through **real integration tests** (no mocks).

1) We don't store context by default, so follow‑up questions will be treated as standalone.

```
First question: First question: How is the climate in Europe in May?
First response: May is generally a pleasant month in Europe as it marks the transition from spring to summer. The climate can vary significantly across different regions:

1. **Western Europe** (e.g., the UK, France, Germany): Temperatures typically range from 10°C to 20°C (50°F to 68°F). Rain is possible, but there are many sunny days, making it a lovely time for outdoor activities.

2. **Southern Europe** (e.g., Spain, Italy, Greece): May tends to be warm and dry, with temperatures ranging from 15°C to 25°C (59°F to 77°F) or even higher in southern areas. It's a popular time for tourists as the weather is generally sunny and pleasant.

3. **Northern Europe** (e.g., Scandinavia, Finland): In northern regions, May can still be relatively cool, with temperatures ranging from 5°C to 15°C (41°F to 59°F). However, you can expect longer daylight hours, which adds to the charm of spring.

4. **Eastern Europe** (e.g., Poland, Hungary, Romania): Temperatures can vary widely but typically fall between 10°C and 20°C (50°F to 68°F). Rain is possible, but there are also many sunny days.

Overall, May is a great time to visit Europe due to mild weather and blooming landscapes. However, it's advisable to check the specific climate of the region you plan to visit, as conditions can vary.

Second question: What should I pack for a trip in such a climate?

(no context) Second response: To provide you with the best packing advice, I'll need to know the specific climate or destination you are referring to. However, here are some general packing tips based on different climates: ...
```

2) With memory enabled, the assistant can reference previous interactions:

```
First question: How is the climate in Europe in May?
In May, the climate in Europe generally varies by region, but it is typically characterized by the following:

1. **Western Europe**: Countries like the UK, France, and Germany experience mild temperatures, usually ranging from 10°C to 20°C (50°F to 68°F). Rainfall is common, but there are also sunny days.

2. **Southern Europe**: Countries such as Spain, Italy, and Greece begin to warm up significantly, with temperatures often reaching between 15°C to 25°C (59°F to 77°F). It is generally drier, and the likelihood of rain decreases.

3. **Eastern Europe**: Nations like Poland and Hungary see mild weather, with temperatures varying from 10°C to 20°C (50°F to 68°F). Rain is possible, but spring blooms make it a picturesque time.

4. **Northern Europe**: Countries such as Sweden and Norway can still be cool, with temperatures ranging from 5°C to 15°C (41°F to 59°F). There may still be some late spring frosts in some areas.

Overall, May is regarded as a pleasant month across much of Europe, as people enjoy the transition from spring to early summer.

Second question: What should I pack for a trip in such a climate?
(SupportChat with memory) Second response: For a trip to Europe in May, it's advisable to pack the following items to accommodate the varying climates:

1. **Clothing**:
   - Light layers: T-shirts, long-sleeve shirts, and a lightweight sweater or cardigan for cooler temperatures.
   - A medium-weight jacket: A waterproof or wind-resistant jacket is beneficial for rainy days.
   - Pants: Comfortable trousers or jeans; consider packing some shorts if you are heading to southern Europe.
   - Dresses or skirts: Lightweight options can be nice for warmer regions.

2. **Footwear**:
   - Comfortable walking shoes: Sneakers or walking shoes are ideal for exploring.
   - Waterproof shoes or boots: Especially useful in regions where rain is expected.

3. **Accessories**:
   - Umbrella or raincoat: Always a good idea, especially in western and northern Europe.
   - Sunglasses and sun hat: For sunnier days, particularly in southern Europe.
   - Scarf or shawl: Useful for cooler evenings.

4. **Travel Essentials**:
   - Travel adapter: Europe uses different plug types, so an adapter is necessary for electronics.
   - Personal toiletries: Basic items, but remember to check airline restrictions on liquids.
   - Lightweight backpack or day bag: Handy for daily outings.

5. **Optional**:
   - Swimwear: If you plan to visit beaches or swimming pools.
   - Camera or smartphone: To capture travel memories.

Packing versatile clothing that can be layered will help prepare for the varying weather conditions across different regions in Europe during May.
```

3) For structured output, we get a reliable JSON response that can be parsed into a Java record:
```
Request: "Tell me about Boeing 777X. I need manufacturer, model, and max distance.
Aircraft response: AircraftResponse[manufacturer=Boeing, model=777X, maxDistance=7750.0]
```

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
