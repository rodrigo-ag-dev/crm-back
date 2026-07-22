-- Global (shared) schema: tenant registry and the single, cross-tenant identity table.
-- Business data lives in per-tenant schemas (see db/migration/tenant), one physical
-- Postgres schema per tenant, selected at request time via Hibernate multi-tenancy.
--
-- public.tenant only ever stores the tenant's short "slug" - its physical Postgres
-- schema name (crm_<slug>) is computed in code (TenantSchemaNames.forSlug) and never
-- persisted, so the "crm_" naming convention lives entirely in the application layer.

CREATE TABLE IF NOT EXISTS public.tenant (
    id VARCHAR(36) PRIMARY KEY,
    slug VARCHAR(63) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public."user" (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL REFERENCES public.tenant(id),
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

-- One-time move of pre-existing single-tenant data (the legacy "crm" schema, from
-- before multi-tenancy existed) into the new global tables. Registers it as the first
-- tenant, with slug "default" (so its physical schema becomes "crm_default", following
-- the same crm_<slug> convention as every other tenant - see TenantSchemaNames). No-op
-- on a fresh install where the legacy schema never existed.
-- V2 in this same migration set finishes the job: it repoints crm.* foreign keys at
-- public."user", drops the now-redundant crm."user" table, and renames the schema
-- itself to crm_default (kept as a separate step there since it must happen only after
-- crm.* still refers to itself as "crm" for the FK/table lookups to resolve).
DO $$
DECLARE
  v_tenant_id VARCHAR(36);
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'crm' AND table_name = 'user') THEN
    SELECT id INTO v_tenant_id FROM public.tenant WHERE slug = 'default';

    IF v_tenant_id IS NULL THEN
      v_tenant_id := gen_random_uuid()::text;
      INSERT INTO public.tenant (id, slug, name) VALUES (v_tenant_id, 'default', 'Default');
    END IF;

    INSERT INTO public."user"
      (id, tenant_id, username, email, full_name, password_hash, active, role, must_change_password, updated_at, created_at)
    SELECT u.id, v_tenant_id, u.username, u.email, u.full_name, u.password_hash, u.active, u.role,
           u.must_change_password, u.updated_at, u.created_at
    FROM crm."user" u
    WHERE NOT EXISTS (SELECT 1 FROM public."user" pu WHERE pu.id = u.id);
  END IF;
END $$;
