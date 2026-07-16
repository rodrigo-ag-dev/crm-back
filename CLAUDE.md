# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

All commands run from `crm-back/api/`. Use `gradlew.bat` on Windows shells, `./gradlew` in Git Bash/WSL.

```
./gradlew bootRun                                    # run the API on :8080 (loads .env automatically)
./gradlew compileJava                                # compile only, fast feedback on type errors
./gradlew test                                       # run the full test suite (loads .env automatically)
./gradlew test --tests "AuthControllerIT"             # run one test class
./gradlew test --tests "AuthControllerIT.loginSetsHttpOnlySessionCookieAndMeReturnsUser"  # run one test method
./gradlew build                                       # compile + test + package
```

Tests are **integration tests that hit a real local PostgreSQL database** (no Testcontainers, no H2) — the same instance configured in `.env`. Each test class is `@Transactional`, so writes roll back automatically after every test; it's safe to re-run repeatedly without manual cleanup.

## Architecture: hexagonal / ports & adapters

```
domain/
  model/        JPA entities (Deal, Ticket, Company, Contact, User, Stage, ...) — extend BaseModel (UUID id, createdAt/updatedAt)
  dto/          Request/response DTOs, decoupled from entities. Bean Validation annotations live here.
  exception/    BusinessException (400), NotFoundException (404), UnauthorizedException (401)
ports/
  in/           Use-case interfaces (e.g. DealPortIn) — what the application can do
  out/          Persistence interfaces (e.g. DealPortOut) — what the application needs from storage
application/    Services implementing ports/in (DealService, TicketService, UserService, ...) — business logic lives here
infrastructure/
  controller/   REST controllers — thin, delegate straight to a ports/in service
  persistence/  Adapters implementing ports/out on top of Spring Data repositories
  repository/   Spring Data JPA repositories
  security/     JWT issuing/validation, auth cookie, rate limiting, RBAC helpers (see below)
  config/       RestExceptionHandler (@RestControllerAdvice → RFC 7807 ProblemDetail responses)
config/         SecurityConfig (filter chain, CORS, method security), JwtProperties (unused, kept for reference)
```

A new feature typically touches five files: entity in `domain/model`, DTO in `domain/dto`, port interfaces in `ports/in`/`ports/out`, service in `application/`, controller in `infrastructure/controller/`, adapter in `infrastructure/persistence/`.

## Auth flow

- `POST /api/auth/login` authenticates via `AuthenticationManager`, then sets an httpOnly/Secure/SameSite=Lax cookie named `crm_token` (see `AuthController` + `AuthCookie`). The response body carries the user object, never the raw token.
- `JwtAuthenticationFilter` reads the token from the `Authorization: Bearer` header **or** the `crm_token` cookie (cookie is what the frontend actually uses).
- `POST /api/auth/logout` adds the token to `TokenBlocklist` (in-memory, keyed by raw token string, expires with the token's own TTL) so a captured/replayed token stops working immediately — clearing the cookie alone wouldn't do that since JWTs are stateless.
- `GET /api/auth/me` returns the current user; the frontend calls this on every page load to restore session state (it can't read the httpOnly cookie itself).
- `AuthRateLimitFilter` + `RateLimiter` (in-memory, fixed-window, keyed by client IP) throttle `/api/auth/login` (10/5min) and `/api/auth/register` (5/hour). Both `RateLimiter` and `TokenBlocklist` are single-instance/in-memory — a multi-instance deployment would need a shared store (Redis) instead.
- RBAC: `CustomUserDetailsService` grants `ROLE_<user.role>` (`ADMIN` or `USER`). `@EnableMethodSecurity` is on; admin-only endpoints use `@PreAuthorize("hasRole('ADMIN')")` (see `UserController.listUsers`, `StageController`/`ParameterController` mutation endpoints). `CurrentUserProvider.requireSelfOrAdmin(userId)` guards per-user endpoints (e.g. user-specific parameters) where a plain role check isn't enough.
- Ownership scoping for Deal/Ticket is **not** a security-layer concern — it's baked into the repository queries themselves (`findByIdAndOwnerId`) and a private `getCurrentUserId()` helper in each service that resolves `SecurityContextHolder`'s authenticated email to a `User.id`. If you add a new Deal/Ticket endpoint, follow that existing pattern rather than trusting a `userId`/`ownerId` field from the request body.

## Database

Flyway migrations in `src/main/resources/db/migration/`, applied to the `crm` schema (`spring.flyway.default-schema=crm`). Migration files are append-only — never edit a shipped migration; add a new `V{n}__description.sql`.
