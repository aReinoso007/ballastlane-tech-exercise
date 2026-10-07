# Pokedex - Ballastlane Tech Exercise

A full-stack Pokemon application built to the exercise brief in [`docs/documentation.md`](docs/documentation.md):

- **Backend** - Java 17, Spring Boot 3, Clean Architecture, TDD, PostgreSQL, JWT auth, PokeAPI integration with caching.
- **Frontend** - React 19 + TypeScript, Tailwind CSS, TanStack Query, React Router.
- **Delivery** - Dockerfiles, `docker-compose.yml`, GitHub Actions CI, seeded demo data and a demo account.

```text
ballastlane-tech-exercise/
  backend/            Spring Boot API (Gradle)
  frontend/           Vite + React + TypeScript SPA
  docs/               Brief, architecture, GenAI write-up, presentation notes
  docker-compose.yml  postgres + backend + frontend
```

## Quick start (Docker)

Requirements: Docker with Compose v2.

```bash
docker compose up --build
```

| What | URL |
| --- | --- |
| Web app | http://localhost:3000 |
| API | http://localhost:8089/api |
| Swagger UI | http://localhost:8089/swagger-ui.html |
| Health | http://localhost:8089/actuator/health |

**Demo account:** `demo` / `Demo1234!` (created on startup). Ten Pokemon (Bulbasaur, Charmander, Squirtle lines and Pikachu) are pre-synced into the local database so "My Pokemon" is not empty on first run.

Ports and secrets can be overridden through environment variables, see [`.env.example`](.env.example). Stop with `docker compose down` (add `-v` to wipe the database).

## Local development

Requirements: JDK 17, Node 20+, Docker (for Postgres and for Testcontainers in the tests).

```bash
# 1. database
docker compose up -d postgres

# 2. backend (http://localhost:8089)
cd backend
./gradlew bootRun

# 3. frontend (http://localhost:5173, proxies /api to the backend)
cd frontend
npm install
npm run dev
```

If your default JDK is not 17, set `JAVA_HOME` before running Gradle (the build uses a Java 17 toolchain).

### Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | local Postgres | Datasource |
| `SERVER_PORT` | `8089` | Backend port |
| `JWT_SECRET` | dev-only value | HS256 key, **min 32 chars - always override outside local dev** |
| `JWT_EXPIRATION_MINUTES` | `120` | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Allowed browser origins |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | Upstream catalog |
| `SEED_DEMO_USER` | `true` | Create the demo account on startup |
| `VITE_DEV_API_TARGET` | `http://localhost:8089` | Vite dev proxy target |

## Tests

```bash
cd backend  && ./gradlew test          # unit + integration (Testcontainers needs Docker); report: build/reports/jacoco
cd frontend && npm test                # Vitest + Testing Library + MSW
cd frontend && npm run lint && npm run typecheck && npm run build
```

Backend tests, by layer:

| Layer | Style |
| --- | --- |
| `domain` | Plain JUnit - invariants and edit semantics of `Pokemon` |
| `application` | Plain JUnit with in-memory fakes / Mockito ports - one test class per user story |
| `infrastructure` | `MockRestServiceServer` for the PokeAPI client (incl. cache behaviour), JSON fixtures for the mapper, `@DataJpaTest` against a real Postgres (Testcontainers), JWT unit tests |
| `web` | Full `@SpringBootTest` + MockMvc + Postgres, only PokeAPI is stubbed - covers security rules, validation and error mapping end to end |
| Architecture | ArchUnit rules enforce the dependency rule |

## User stories and where they live

| Story | Endpoint(s) | Frontend |
| --- | --- | --- |
| US01 Paginated list (sprite, category, weight, abilities) | `GET /api/pokemon?page=&size=` | Home page |
| US02 Detail (image, stats, description, evolutions) | `GET /api/pokemon/{idOrName}` | `/pokemon/:name` |
| US03 Sync to local DB | `POST /api/local/pokemon/{idOrName}/sync`, `GET /api/local/pokemon[/{id}]` | "Save to my Pokedex", My Pokemon |
| US04 Modify local data | `PUT` / `PATCH /api/local/pokemon/{id}`, `DELETE ...` | Edit page, delete dialog |
| Users | `POST /api/auth/register`, `POST /api/auth/login` | Sign in / Register |

Public routes: `/api/auth/**`, `GET /api/pokemon/**`, Swagger, health. Everything under `/api/local/**` requires `Authorization: Bearer <jwt>`.

Errors use RFC 7807 (`application/problem+json`):

| Status | When |
| --- | --- |
| 400 | Malformed JSON, unknown fields, failed validation (`errors` lists each violation) |
| 401 / 403 | Missing, invalid or expired token / insufficient rights |
| 404 | Pokemon unknown in PokeAPI or in the local database |
| 409 | Pokemon already synced, username/email already taken |
| 502 | PokeAPI unreachable or returned unusable data |

Example:

```bash
TOKEN=$(curl -s localhost:8089/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"Demo1234!"}' | jq -r .token)

curl -X POST  localhost:8089/api/local/pokemon/eevee/sync -H "Authorization: Bearer $TOKEN"
curl -X PATCH localhost:8089/api/local/pokemon/133 -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"region":"Kanto","tags":["normal"]}'
```

## Architecture in one minute

Dependencies point inwards only (`web` -> `application` -> `domain`; `infrastructure` implements the domain ports). See [`docs/architecture.md`](docs/architecture.md) for diagrams and the decisions behind them.

- `domain` is framework-free: the `Pokemon` aggregate validates itself, ports are interfaces.
- `application` holds one class per use case (`SyncPokemon`, `UpdateLocalPokemon`, ...), also framework-free and wired in `UseCaseConfig`.
- `infrastructure` contains the PokeAPI client (+ Caffeine cache), JPA/Flyway persistence and JWT security.
- `web` contains controllers, DTOs and the global exception handler.

## Other documents

- [`docs/architecture.md`](docs/architecture.md) - design and trade-offs
- [`docs/genai.md`](docs/genai.md) - GenAI exercise (prompt, output sample, validation)
- [`docs/presentation.md`](docs/presentation.md) - talk track, demo script and Q&A prep
