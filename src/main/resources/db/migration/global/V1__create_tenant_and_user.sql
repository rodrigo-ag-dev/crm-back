-- Global (shared) schema: tenant registry and the single, cross-tenant identity table.
-- Business data lives in per-tenant schemas (see db/migration/tenant), one physical
-- Postgres schema per tenant, selected at request time via Hibernate multi-tenancy.
--
-- crm_setup.tenant only ever stores the tenant's short "slug" - its physical Postgres
-- schema name (crm_<slug>) is computed in code (TenantSchemaNames.forSlug) and never
-- persisted, so the "crm_" naming convention lives entirely in the application layer.
-- "crm_setup" itself is reserved (see TenantSchemaNames) so no tenant slug can ever
-- collide with it (a tenant slug of "setup" would otherwise produce that exact name).

CREATE TABLE IF NOT EXISTS crm_setup.tenant (
    id VARCHAR(36) PRIMARY KEY,
    slug VARCHAR(63) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS crm_setup."user" (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL REFERENCES crm_setup.tenant(id),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, username)
);
