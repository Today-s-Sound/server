# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java`: application source (Spring Boot, packages under `com.todaysound...`).
- `src/main/resources`: configuration and assets (`application*.yml`, `logback-spring.xml`, `static/`).
- `src/main/resources/db/migration`: Flyway SQL migrations (e.g., `V2__update_schema.sql`).
- `src/test/java`: unit/integration tests.
- `docs/` and `build/docs/asciidoc`: generated REST Docs output.

## Build, Test, and Development Commands
- `./gradlew bootRun`: run the app locally using the active Spring profile.
- `./gradlew test`: run the full JUnit test suite (includes REST Docs snippets).
- `./gradlew build`: compile, test, and build the boot jar.
- `./gradlew asciidoctor`: generate REST Docs HTML from snippets.
- `./gradlew copyDocument`: copy generated docs into `docs/` (used by `bootJar`).

## Coding Style & Naming Conventions
- Java: 4-space indentation, standard Spring Boot conventions for packages and classes.
- Tests: class names end with `*Test` or `*IntegrationTest` (JUnit 5).
- Flyway: use `V{number}__{description}.sql` in `src/main/resources/db/migration`.
- No formatter/linter configured in Gradle; keep changes consistent with nearby code.

## Testing Guidelines
- Frameworks: JUnit Jupiter, Spring Boot Test, Testcontainers, Spring REST Docs.
- Favor integration tests for controller/service behavior; use Testcontainers for MySQL when needed.
- Run `./gradlew test` before opening a PR; REST Docs snippets are generated under `build/generated-snippets`.

## Commit & Pull Request Guidelines
- Commit style in history follows short conventional prefixes (e.g., `feat: ...`, `fix: ...`).
- Keep commits scoped to one change area and describe the impact clearly.
- PRs should include: purpose, testing performed (`./gradlew test` or targeted task), and any schema changes (link to Flyway migration).
- If API behavior changes, update REST Docs and include a brief note in the PR.

## Configuration & Security Notes
- Environment-specific settings live in `application-local.yml` and `application-prod.yml`.
- Do not commit secrets; use env vars or external config for credentials.
