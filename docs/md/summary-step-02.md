# Power Tools — LangChain4j Quick-Start (Chapter 5)

## Summary

This chapter is a hands-on introduction to LangChain4j, a Java framework that simplifies building LLM-powered apps (chatbots/assistants) by providing a unified API and reusable building blocks. The chapter’s goal is to get you from zero to a working Spring Boot endpoint that calls an LLM, and sets the stage for later improvements like prompt engineering and RAG.

## What LangChain4j is (and why it exists)

LangChain is a popular LLM orchestration framework in Python.

LangChain4j is the Java equivalent, designed for Java ecosystems (notably Spring Boot and Quarkus).

It helps with common LLM-app needs such as:

- Prompt templating

- Chat memory

- Output parsing

- (Later) retrieval and tool calling patterns

## What you build in the chapter

A simple “TravelX” airline-style assistant backend (API-only; no UI) where:

- A client sends a message to a Spring Boot REST endpoint

- The controller uses LangChain4j

- LangChain4j calls OpenAI

- The response is returned to the client

Architecture is intentionally minimal: Customer → REST Controller → LangChain4j → OpenAI model.

## Setup & prerequisites

- Java 17+

- Gradle/Maven

- IDE of choice

You bootstrap a Spring Boot app (via Spring Initializr) and add LangChain4j dependencies:

- langchain4j-open-ai

- langchain4j

# Key implementation steps (quick-start flow)

1. Configure API key in application.properties

- Uses openai.api-key=demo for testing (note: env vars are recommended in real apps)

2. Create a Spring config bean that builds an OpenAiChatModel

- Selects a lightweight model: gpt-4o-mini

- This bean becomes the app’s LLM client

3. Define request shape with a simple ChatRequest POJO

- Holds a single message field

4. Create the REST endpoint /api/chat

- POST endpoint reads message

- Calls chatModel.generate(message)

- Returns the model output directly

5. Test with curl

- POST "Say Hello World" → returns "Hello, World!"

## Takeaways

- LangChain4j gives Java devs a clean entry point into LLM apps without hand-rolling everything.

- The chapter intentionally starts with the simplest “call an LLM” setup to establish the baseline.

- Next chapters build on this by adding better prompting, RAG, and richer orchestration patterns.