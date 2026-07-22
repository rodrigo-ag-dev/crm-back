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
  tenant/       Schema-per-tenant multi-tenancy: TenantContext, MultiTenantConnectionProvider, TenantIdentifierResolver, TenantFlywayMigrator, TenantMigrationRunner (see below)
  config/       RestExceptionHandler (@RestControllerAdvice → RFC 7807 ProblemDetail responses)
config/         SecurityConfig (filter chain, CORS, method security), MultiTenancyConfig (wires the tenant infra into Hibernate), JwtProperties (unused, kept for reference)
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

## Multi-tenancy (schema per tenant)

Business entities (Deal, Ticket, Company, Contact, Stage, TicketStage, Parameter, UserParameter) live in a
**per-tenant Postgres schema**, resolved dynamically per request. `Tenant` and `User` are the only entities pinned
to the shared `public` schema (`@Table(schema = "public")`) — identity has to be resolvable before any tenant
schema is known (e.g. at login), and email is unique across the whole platform, not per tenant.

- **Request-time resolution**: `JwtAuthenticationFilter`, right after building the `AppUserPrincipal` (which
  `CustomUserDetailsService` loads with the user's `tenantId`/`tenantSchema`), sets `TenantContext` (a ThreadLocal)
  for the duration of the request and clears it in a `finally` block. `TenantIdentifierResolver` (Hibernate's
  `CurrentTenantIdentifierResolver`) reads it; `SchemaMultiTenantConnectionProvider` runs `SET search_path` on the
  JDBC connection Hibernate acquires for that unit of work, and resets it to `public` before the connection returns
  to the Hikari pool — resetting on release is essential, since pooled connections are reused across tenants.
- **Fallback**: outside an authenticated request (tests calling repositories directly, startup jobs),
  `TenantIdentifierResolver` falls back to `app.tenant.default-schema` (default `crm`) rather than `public`, since
  `public` has no business tables. This never fires on real authenticated traffic — every business request has
  `TenantContext` set before touching a tenant-scoped repository.
- **Never trust a client-supplied tenant.** The tenant for a write always comes from the authenticated principal
  (`CurrentUserProvider.getCurrentUser().getTenantId()`), never from a request body field — see
  `UserService.createUser` for the pattern (an admin creating a user always stamps the admin's own tenant).
- **Naming**: schemas provisioned via `POST /api/tenants` are namespaced `crm_<slug>` (e.g. slug `acme` ->
  schema `crm_acme`, see `TenantSchemaNames.forSlug`), so tenant schemas are easy to pick out from other
  schemas/apps sharing the same Postgres instance. The pre-existing `crm` schema (the auto-registered default
  tenant) predates this convention and is deliberately left unprefixed — it already holds real data and a rename
  isn't required for anything to work. `tenantSlug` fields elsewhere (`POST /api/setup`, `POST /api/auth/register`)
  expect the full schema name (`crm_acme`), not the bare slug used only when creating the tenant.
- **Provisioning**: `TenantProvisioningService.createTenant` inserts a `public.tenant` row, then
  `TenantFlywayMigrator` runs the tenant migration set against that schema (Flyway creates the schema if missing).
  Idempotent by slug — safe to retry. `TenantMigrationRunner` (an `ApplicationRunner`) does the same for every
  registered tenant on every boot, so newly added tenant migrations reach already-provisioned tenants, and
  auto-creates a default tenant on a fresh install so local dev needs no extra provisioning step.
- **Schema names are never string-concatenated without validation** — `TenantSchemaNames.isValid` (allowlist regex
  + reserved-word blocklist) gates every path that turns a slug into literal SQL (`SET search_path`, Flyway's
  `schemas(...)`), since JDBC has no way to bind an identifier as a bound parameter.

## Database

- **Global** migrations (`src/main/resources/db/migration/global/`) manage the shared `public` schema (`tenant`,
  `user`) via Spring Boot's auto-configured Flyway (`spring.flyway.schemas=public`).
- **Tenant** migrations (`src/main/resources/db/migration/tenant/`) are the template applied to every tenant's own
  schema by `TenantFlywayMigrator`/`TenantMigrationRunner` (own schema-history table, `flyway_tenant_history`, per
  schema). Table names in this location are intentionally **unqualified** (no `crm.` prefix) since the same file
  runs against whichever schema Flyway is pointed at for that run; foreign keys to the user table are qualified as
  `public."user"(id)` since identity is shared, not per-tenant.
- **Legacy** (`src/main/resources/db/migration/legacy/`) holds the original single-schema `V1__create_full_schema.sql`
  for historical reference only — no active Flyway config points at it anymore.
- Migration files are append-only within each of these sets — never edit a shipped migration; add a new
  `V{n}__description.sql` in the appropriate location.
