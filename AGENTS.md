# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/example/shopmark1` follows a vertical slice layout; each domain (`product`, `user`, `order`, `payment`) groups its `domain`, `application`, and `presentation` packages by responsibility.
- `src/main/resources` stores Spring configuration, SQL assets, and generated docs mirrored to `build/resources/main/static/springdoc`.
- Keep tests in `src/test/java`, mirroring production packages; share reusable fixtures under the `global` helpers.

## Build, Test, and Development Commands
- `./gradlew clean build` cleans output, compiles with Java 17, runs unit/integration tests, and regenerates API docs.
- `./gradlew bootRun` boots the application with the default profile (H2 + seed data) for local verification.
- `./gradlew test` executes the test suite without rebuilding docs; run before opening a pull request.
- `./gradlew setDocs` refreshes REST Docs and OpenAPI artifacts into `build/api-spec` for reviewers.

## Coding Style & Naming Conventions
- Use 4-space indentation and Lombok builders/getters to keep entities and DTOs concise; avoid manual accessor code.
- Packages stay domain-first (`product.presentation.controller`, `global.infrastructure.config`); align DTOs and services with the same version suffix (`V1`).
- REST endpoints should return `ResponseEntity<ApiDto<...>>`, wrapping payloads with `ApiDto.builder().data(...)`.
- Name request/response DTOs using `Req...DtoV1` and `Res...DtoV1`, and model primary keys with `UUID`.

## Testing Guidelines
- Prefer Spring Boot + JUnit 5 with MockMvc; scope tests using `@WebMvcTest` for controllers and `@SpringBootTest` for full flows.
- Mirror production packages under `src/test/java`; end test classes in `Tests` with methods in `shouldDoXWhenY` format.
- Regenerate REST Docs snippets when API contracts shift so the published OpenAPI spec stays trustworthy.

## Commit & Pull Request Guidelines
- Follow Conventional Commits (`feat:`, `fix:`, `chore:`); keep the subject imperative and under 72 characters.
- Limit each pull request to one logical change; include a summary, test evidence, and linked issue or ticket.
- Capture breaking API or UI updates with screenshots or refreshed docs, and verify `./gradlew build` locally before review.

## Security & Configuration Notes
- Keep secrets in environment variables or profile-specific `application-*.yml` files; never commit credentials.
- Authentication logic resides under `global.infrastructure.config.security`; reuse existing filters before adding new ones.
- The default profile uses in-memory H2; document any datasource overrides needed for other environments.
