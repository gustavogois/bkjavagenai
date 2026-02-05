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
