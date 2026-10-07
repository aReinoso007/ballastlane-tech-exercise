# GenAI exercise - Task management API

> Prepared answer for the "Generative AI tools" section of the brief. The prompt is written for Cursor (agent mode) but works with any GenAI coding tool. The code below is a **representative sample** of what such a prompt produces, not a file that ships in this repository. Adjust the "what I corrected" section to match your own session before presenting.

## 1. The prompt

A good prompt states the stack, the architecture, the rules and the acceptance criteria up front, and asks for tests first. This is the one I would use:

```text
You are helping me build a RESTful task-management API.

## Stack
Java 17, Spring Boot 3.3, Gradle, PostgreSQL (Flyway migrations), Spring Security with JWT (stateless).
Tests: JUnit 5, Mockito, Testcontainers (Postgres), MockMvc.

## Architecture
Clean Architecture with four packages: domain (no framework imports), application (use cases, plain Java),
infrastructure (JPA, security), web (controllers, DTOs). Dependencies only point inwards.
Constructor injection only. DTOs are Java records; never expose JPA entities from controllers.

## Domain
Task: id (UUID), title (required, 1-120 chars), description (optional, max 2000), status (TODO | IN_PROGRESS | DONE,
default TODO), dueDate (optional, ISO date, must not be in the past when creating), ownerId.
A User already exists (id, username) and the authenticated user is available from the JWT.

## Behaviour
- POST   /api/tasks            create a task for the authenticated user          -> 201 + Location
- GET    /api/tasks            list MY tasks, paginated, optional ?status= filter -> 200
- GET    /api/tasks/{id}       get one of my tasks                                -> 200 / 404
- PUT    /api/tasks/{id}       replace title, description, status, dueDate        -> 200 / 400 / 404
- PATCH  /api/tasks/{id}       partial update                                     -> 200 / 400 / 404
- DELETE /api/tasks/{id}       delete                                             -> 204 / 404
A user must never see or modify another user's task: answer 404 (not 403) to avoid leaking existence.

## Errors
Return RFC 7807 problem+json with a list of field errors for 400. Unknown fields and malformed JSON are 400.
Missing or invalid token is 401.

## Process
1. Start with the failing tests for the application layer (use cases with in-memory fakes), then implement.
2. Then the persistence adapter test (Testcontainers), then controller tests (MockMvc) including the
   authorization edge cases (other user's task, no token, expired token).
3. Show me the files in dependency order and explain any assumption you make instead of guessing silently.
Do not add features I did not ask for.
```

## 2. Representative output (excerpt)

```java
// domain/Task.java - framework free, validates itself
public record Task(UUID id, String title, String description, TaskStatus status, LocalDate dueDate, UUID ownerId) {

    public Task {
        title = title == null ? null : title.trim();
        List<String> violations = new ArrayList<>();
        if (title == null || title.isEmpty() || title.length() > 120) violations.add("title must be 1-120 characters");
        if (description != null && description.length() > 2000) violations.add("description must be at most 2000 characters");
        if (status == null) violations.add("status is required");
        if (ownerId == null) violations.add("ownerId is required");
        if (!violations.isEmpty()) throw new InvalidTaskException(violations);
    }

    public static Task create(String title, String description, LocalDate dueDate, UUID ownerId, Clock clock) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now(clock))) {
            throw new InvalidTaskException(List.of("dueDate must not be in the past"));
        }
        return new Task(UUID.randomUUID(), title, description, TaskStatus.TODO, dueDate, ownerId);
    }
}

// application/GetTask.java - ownership is enforced in the use case, not in the controller
public class GetTask {
    private final TaskRepository tasks;

    public GetTask(TaskRepository tasks) { this.tasks = tasks; }

    public Task execute(UUID taskId, UUID requesterId) {
        return tasks.findByIdAndOwnerId(taskId, requesterId)          // other users' tasks look like "not found"
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}

// web/TaskController.java
@RestController
@RequestMapping("/api/tasks")
class TaskController {
    @PostMapping
    ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest body,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        Task created = createTask.execute(body.title(), body.description(), body.dueDate(), user.id());
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(TaskResponse.from(created));
    }

    @GetMapping
    PageResponse<TaskResponse> list(@RequestParam(required = false) TaskStatus status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @AuthenticationPrincipal AuthenticatedUser user) {
        return PageResponse.from(listTasks.execute(user.id(), status, page, size), TaskResponse::from);
    }
}

// the first test the tool wrote, before any implementation
@Test
void userCannotReadSomeoneElsesTask() {
    Task mine = repository.save(Task.create("Mine", null, null, alice, clock));

    assertThatThrownBy(() -> new GetTask(repository).execute(mine.id(), bob))
            .isInstanceOf(TaskNotFoundException.class);
}
```

## 3. How I validated the output

| Check | How |
| --- | --- |
| It compiles and the tests are meaningful | Ran the suite; then **broke the code on purpose** (removed the owner filter, dropped a validation) and confirmed a test failed. Tests that cannot fail are decoration. |
| Architecture rules | An ArchUnit test fails the build if `domain`/`application` import Spring or if `web` imports `infrastructure`. AI tools drift on layering unless something enforces it. |
| API contract | Exercised every endpoint with `curl`/Swagger UI: happy paths plus 400/401/404, malformed JSON, unknown fields, huge payloads. |
| Dependencies | Checked that every suggested library/version exists and is maintained; removed anything unused. |
| Security review | Read the filter chain line by line: which routes are public, how the token is verified, what happens on expiry. |
| Line-by-line review | Treated the diff like a pull request from a junior colleague: naming, duplication, error messages, leaked internals. |

## 4. What I corrected or improved

Typical problems in first-pass AI output, and the fix applied:

1. **Entities leaked through controllers.** Replaced with response records so the API contract is independent of the schema.
2. **Ownership checked in the controller (or not at all).** Moved into the use case with a repository query `findByIdAndOwnerId`, so no code path can forget it.
3. **403 instead of 404 for other users' tasks.** Changed to 404 to avoid confirming that an id exists.
4. **`LocalDate.now()` called directly.** Injected a `Clock` so the "due date in the past" rule is testable.
5. **Validation duplicated** in DTO annotations and service. Kept Bean Validation for shape (required, size) and put business rules in the domain object; the error handler merges both into one problem+json format.
6. **PUT and PATCH treated the same.** PUT now requires every editable field; PATCH changes only the fields present and rejects an empty body.
7. **Stack traces and SQL messages in error bodies.** Added a catch-all handler that logs the cause and returns a generic message.
8. **Secrets in `application.yml`.** Moved the JWT secret to an environment variable with a length check at startup.

## 5. Edge cases, authentication and validation

- **Authentication**: stateless JWT (HS256), BCrypt password hashes, `401` for missing/expired/tampered tokens, public routes limited to login/register, docs and health.
- **Authorization**: every query is scoped by `ownerId`; tested with two users.
- **Validation**: title length, description length, status must be a valid enum value (bad value -> 400, not 500), `dueDate` ISO format, not in the past on creation.
- **Edge cases covered by tests**: empty/whitespace title, 121-char title, unknown status, malformed JSON, unknown JSON fields, non-UUID path id, pagination bounds (negative page, size 0 or too large), deleting twice, updating a deleted task, concurrent updates (optimistic locking with `@Version`), unauthenticated access to each endpoint.
- **What AI is not trusted with**: security configuration and anything touching secrets get a manual review every time; AI output is a draft, I own the result.

## 6. How this repository was actually built (process log)

This section is the real story of the Pokedex project, not a template. Tool: Cursor, Plan mode first, then Agent mode.

### 6.1 Context first, then a plan

1. I converted the exercise PDF to Markdown and deleted everything that was not needed for the coding task, so the agent only sees requirements and not noise.
2. I opened the agent in **Plan mode** and gave it one prompt: *"I want to tackle the following exercise described in @documentation.md, check it out and generate a plan to achieve this. I want a mono-repo with the name ballastlane-tech-exercise. I have already scaffolded the Java application, check that all is good and well."*
3. The agent reviewed my scaffold and asked two questions that were mine to decide: which relational database (I chose PostgreSQL) and the repo layout (a new `ballastlane-tech-exercise` folder, copying only the useful parts of my scaffold and leaving the old folder untouched).
4. I reviewed the plan and approved it with explicit rules: implement it as written, do not edit the plan file, work through the existing to-do list in order, and do not stop until every item is done.

### 6.2 Implementation loop

The agent worked layer by layer (domain and application, infrastructure, web, frontend, delivery). After each layer it ran the tests and committed. Guard-rails did the checking so I did not have to trust the generated code: ArchUnit for the dependency rule, Testcontainers against real PostgreSQL, MockMvc for the API contract, MSW for the frontend.

Things the agent got wrong and that the guard-rails or running the app exposed:

- The ArchUnit pattern `..web..` also matched `org.springframework.web`, so the rule checked the wrong thing. Fixed with fully qualified package names.
- A test configuration was picked up by component scanning and caused a duplicate-bean error. Fixed with `@TestConfiguration`.
- Two beans implemented `TokenIssuer`, which caused a `NoUniqueBeanDefinitionException`. Removed the extra bean.
- Ports 8080 and 8081 were already taken on my machine, so the default API port became 8089.
- The Docker base image had no arm64 build; switched to a multi-arch image.
- A first draft of the docs claimed tests were written before the code in the commit history. Commits group tests with the code that makes them pass, so the claim was softened to what is true.

### 6.3 Verification and my follow-up requests

After the build, the agent ran the full Docker Compose stack and clicked through the UI in the browser while capturing console errors and warnings (none). Then I used the app myself and sent follow-up requests:

1. **"Whenever I search for a new Pokemon, as I go through the evolutions there is a brief second where the screen goes blank with a loading screen. After I have gone through them there is no flickering."**
   - Diagnosis: the first visit to each Pokemon is a cache miss in both TanStack Query and the backend (which calls PokeAPI). The detail page returned a full-page spinner while waiting. Later visits were cached, which explains why it only happened once per Pokemon.
   - Fix: keep the previous Pokemon on screen while the next one loads (`placeholderData: keepPreviousData`), and prefetch the rest of the evolution line, plus hover/focus prefetch on links and cards. Two tests were added.
2. **"The flicker is still there when I switch between evolutions on a new Pokemon, data that is not cached."**
   - Diagnosis, this time measured instead of guessed: my running container was still serving the build from before the first fix, so I rebuilt it and recorded the page state with a `MutationObserver` during an uncached evolution click. The blank page and spinner were gone, but two smaller effects remained: dimming and un-dimming the page, and the sprite showing an empty box while the new image downloaded.
   - Fix: removed the dimming, added a thin progress bar that only appears after 250 ms (so quick loads never flash it), made sprites fade in over a placeholder, and warmed the browser image cache during prefetch. Re-measured: the page always had a heading, never a spinner.
   - Lesson I would repeat: a unit test proves the logic, but UI smoothness has to be checked in the real running build. The same check also showed the frontend container reported "unhealthy" because its healthcheck used `localhost`, which resolves to IPv6 inside the image while nginx listens on IPv4. Switched it to `127.0.0.1`.

### 6.4 What I take from this

AI wrote most of the code, but the outcome depended on four things I did: giving it clean context, choosing the decisions that were mine (database, layout), insisting on guard-rails that fail loudly, and using the product myself and reporting what felt wrong. Each fix started from an observed symptom and a measured cause, not from a guess.
