package com.gois.study.bkjavagenai.config;

import com.gois.study.bkjavagenai.memory.MapDbChatMemoryStore;
import com.gois.study.bkjavagenai.model.Flight;
import com.gois.study.bkjavagenai.rag.FlightDataCatalog;
import com.gois.study.bkjavagenai.service.FlightAssistant;
import com.gois.study.bkjavagenai.service.SupportAssistant;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Set;

/**
 * Central configuration for LangChain4j and OpenAI integration.
 *
 * <p>This class wires together:
 * <ul>
 *   <li>Multiple OpenAI chat models (free-form and JSON-structured)</li>
 *   <li>Embedding model and in-memory embedding store for RAG</li>
 *   <li>Chat memory backed by MapDB</li>
 *   <li>AI assistants built via LangChain4j AiServices</li>
 * </ul>
 *
 * <p>The goal is to keep all AI-related infrastructure configuration
 * explicit, centralized, and discoverable.</p>
 */
@Configuration
public class LangChain4jConfig {

    /**
     * Default OpenAI chat model for free-form conversations.
     *
     * <p>Marked as {@code @Primary} so it is injected by default when no qualifier is specified.</p>
     */
    @Bean
    @Primary
    OpenAiChatModel openAiChatModel(OpenAiProperties props) {
        return OpenAiChatModel.builder()
                .apiKey(props.apiKey())
                .modelName(props.model())
                .baseUrl(props.baseUrl())
                .build();
    }

    /**
     * Structured OpenAI chat model configured to return strict JSON
     * describing aircraft details.
     *
     * <p>This model enforces a JSON schema at the API level, ensuring that
     * responses are machine-parseable and schema-compliant.</p>
     */
    @Bean(name = "structuredOpenAiChatModel")
    OpenAiChatModel structuredOpenAiChatModel(OpenAiProperties props) {
        JsonSchema schema = JsonSchema.builder()
                .name("Aircraft")
                .rootElement(JsonObjectSchema.builder()
                        .addStringProperty("manufacturer", "Aircraft manufacturer")
                        .addStringProperty("model", "Aircraft model")
                        .addNumberProperty("maxDistance", "Maximum distance (nautical miles)")
                        .required("manufacturer", "model", "maxDistance")
                        .additionalProperties(false)
                        .build())
                .build();

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
    }

    /**
     * Structured OpenAI chat model configured specifically for flight extraction.
     *
     * <p>The schema mirrors the {@link com.gois.study.bkjavagenai.model.Flight} domain model
     * and guarantees that all required fields are returned.</p>
     */
    @Bean(name = "flightStructuredOpenAiChatModel")
    OpenAiChatModel flightStructuredOpenAiChatModel(OpenAiProperties props) {
        JsonSchema schema = JsonSchema.builder()
                .name("Flight")
                .rootElement(JsonObjectSchema.builder()
                        .addStringProperty("aircraft", "Aircraft model")
                        .addStringProperty("dateOfDeparture", "Date of departure (YYYY-MM-DD)")
                        .addStringProperty("fromCity", "Origin city")
                        .addStringProperty("toCity", "Destination city")
                        .addStringProperty("departureAirport", "Departure airport code")
                        .addStringProperty("arrivalAirport", "Arrival airport code")
                        .addStringProperty("flightNumber", "Flight number")
                        .addStringProperty("departureTime", "Departure time (HH:mm)")
                        .addStringProperty("arrivalTime", "Arrival time (HH:mm)")
                        .addStringProperty("status", "Flight status")
                        .required(
                                "aircraft",
                                "dateOfDeparture",
                                "fromCity",
                                "toCity",
                                "departureAirport",
                                "arrivalAirport",
                                "flightNumber",
                                "departureTime",
                                "arrivalTime",
                                "status"
                        )
                        .additionalProperties(false)
                        .build())
                .build();

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
    }

    /**
     * Embedding model used for vectorization of documents and queries.
     *
     * <p>This model must be compatible with the embedding store used for RAG.</p>
     */
    @Bean
    EmbeddingModel openAiEmbeddingModel(OpenAiProperties props) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(props.apiKey())
                .modelName(props.embeddingModel())
                .baseUrl(props.baseUrl())
                .build();
    }

    /**
     * Loads static flight documentation and extracts known flight numbers.
     *
     * <p>The extracted flight numbers are later used to constrain or enrich
     * retrieval and reasoning.</p>
     */
    @Bean
    FlightDataCatalog flightDataCatalog() {
        Document document = ClassPathDocumentLoader.loadDocument(
                "static/flight-details.txt",
                new TextDocumentParser()
        );

        Set<String> flightNumbers = Flight.parseFlightNumbers(document.text());
        List<Document> documents = List.of(document);

        return new FlightDataCatalog(documents, flightNumbers);
    }

    /**
     * In-memory embedding store for flight-related documents.
     *
     * <p>Documents are split into overlapping segments to improve retrieval
     * accuracy during semantic search.</p>
     */
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

    /**
     * Persistent chat memory store backed by MapDB.
     *
     * <p>This allows chat context to survive application restarts.</p>
     */
    @Bean
    ChatMemoryStore chatMemoryStore() {
        return new MapDbChatMemoryStore("chat-memory.db");
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();
    }

    /**
     * Provides a per-conversation sliding window chat memory.
     *
     * <p>The window size limits token usage while preserving recent context.</p>
     */
    @Bean
    SupportAssistant supportAssistant(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel,
                                      ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(SupportAssistant.class)
                .chatModel(openAiChatModel)
                .systemMessage(PromptDefaults.SYSTEM_PROMPT)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * AI assistant for general support-style conversations.
     *
     * <p>Uses the default (free-form) chat model and shared chat memory.</p>
     */
    @Bean
    FlightAssistant flightAssistant(@Qualifier("flightStructuredOpenAiChatModel") OpenAiChatModel flightStructuredChatModel,
                                    InMemoryEmbeddingStore<TextSegment> flightEmbeddingStore,
                                    EmbeddingModel embeddingModel) {
        return AiServices.builder(FlightAssistant.class)
                .chatModel(flightStructuredChatModel)
                .systemMessage(PromptDefaults.SYSTEM_PROMPT)
                .contentRetriever(new EmbeddingStoreContentRetriever(flightEmbeddingStore, embeddingModel))
                .build();
    }

}
