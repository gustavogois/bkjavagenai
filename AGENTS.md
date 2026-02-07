# Repository Guidelines

## General
- Use english for all code, comments, and documentation.
- Follow standard Java and Spring Boot conventions for code structure and style.
- Keep the repository organized and clean; remove any generated files or IDE-specific files from version control.
- Use `.gitignore` to exclude `target/`, `.idea/`, and other non-essential files.

## Project Structure & Module Organization
- `src/main/java/com/gois/study/bkjavagenai`: main Spring Boot application code.
- `src/main/resources`: configuration and assets. `application.properties` holds local defaults; `static/` and `templates/` are available for web assets.
- `src/test/java/...`: automated tests.
- `target/`: Maven build output (generated).

## Build, Test, and Development Commands
- `./mvnw clean package`: clean and build the application JAR.
- `./mvnw test`: run the test suite (JUnit 5 via Spring Boot test starter).
- `./mvnw spring-boot:run`: run the app locally with hot reload support if devtools is enabled.

## Coding Style & Naming Conventions
- Java 21 baseline; use standard Spring Boot conventions (constructor injection, `@Configuration`, etc.).
- Indentation: 4 spaces for Java; keep import order consistent with IDE defaults.
- Package names are lowercase and dot-separated (match `com.gois.study.bkjavagenai`).
- Class names use `PascalCase`; tests use `*Tests` suffix.

### Records and Absence Handling

- Java `record`s **must not** expose `empty()`, `blank()`, or similar factory methods that create instances filled with `null`.
- A `record` must always represent a **valid and meaningful domain value**.
- Absence of a value must be modeled using `Optional<T>`, **not** by returning an “empty” record.
- Methods that may not produce a value must return `Optional<RecordType>`.

❌ **Forbidden**
```java
public record Flight(...) {
    public static Flight empty() {
        return new Flight(null, null, ...);
    }
}
```

### Controllers must not perform manual field-by-field mapping - Controller Mapping Rules

- Controllers must remain **thin orchestration layers**.
- Controllers **must not** perform field-by-field mapping between domain objects and response DTOs.
- Mapping logic must be delegated to:
    - a response DTO constructor, or
    - a static factory method (e.g. `from(Domain)`).

- When a service returns `Optional<T>`:
    - Controllers must use `map(...)` to transform the value.
    - Absence must be translated explicitly into the correct HTTP response.

❌ **Forbidden**
```java
.map(flight -> new FlightResponse(
    flight.aircraft(),
    flight.dateOfDeparture(),
    flight.fromCity(),
    flight.toCity(),
))
```

✅ Required
```
.map(FlightResponse::new)
.map(ResponseEntity::ok)
.orElseGet(() -> ResponseEntity.noContent().build());
```

For endpoints returning 204 No Content, ResponseEntity.of(Optional) must not be used.

📌 Rule: Prefer static from(...) factory methods over constructors for DTO mapping
### DTO Construction Rules

- DTOs and response records must **prefer static factory methods** (e.g. `from(DomainType)`)
  over overloaded or convenience constructors.
- Constructors of records must reflect **the data shape**, not mapping logic.
- Mapping from domain objects to DTOs must be:
    - explicit,
    - named (`from(...)`),
    - and discoverable via autocomplete.

❌ **Forbidden**
```java
public record FlightResponse(...) {
    public FlightResponse(Flight flight) {
        this(...);
    }
}
```

✅ Required
```
public record FlightResponse(...) {
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(...);
    }
}
```

Static from(...) methods improve readability, evolvability, and intent clarity.

Controllers must reference mapping via method references when possible:
```
.map(FlightResponse::from)
```

---

## 🧠 Design intent (what this prevents)

- Avoids constructor overload abuse
- Keeps records semantically pure
- Makes mappings easy to locate and change
- Reduces accidental coupling between domain and transport layers

---

## 🧠 Design intent (implicit but enforceable)

- Controllers coordinate, they don’t translate
- Mapping is explicit, reusable, and testable
- `Optional` drives control flow, not null checks
- HTTP semantics are intentional (204 ≠ 404)

## Testing Guidelines
- Framework: Spring Boot WebMVC test starter (JUnit 5).
- Place tests under `src/test/java` mirroring main package paths.
- Name tests with `*Tests` and focus on controller/service behavior.
- Run with `./mvnw test` before opening a PR.

## Commit & Pull Request Guidelines
- Current history contains only an “Initial commit”; no enforced convention yet.
- Use short, imperative commit messages (e.g., “Add chat controller”).
- PRs should include: a concise description, testing notes (`./mvnw test` or rationale), and linked issues if applicable.
- Include screenshots for any UI changes under `src/main/resources/static` or `templates`.

## Configuration Tips
- Local config lives in `src/main/resources/application.properties`.
- Prefer environment variables for secrets; do not commit API keys.
