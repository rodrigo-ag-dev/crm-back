-- Platform admin: a user who can see/manage users across every tenant, independent of
-- their own per-tenant Role (ADMIN/USER). Deliberately a separate flag rather than a
-- third Role value, since it's a different axis (cross-tenant reach vs. in-tenant
-- privilege) - a platform admin is still just ADMIN or USER within their own tenant.
--
-- No API grants this - same precedent as the very first tenant admin (see CLAUDE.md):
--   UPDATE crm_setup."user" SET platform_admin = true WHERE email = '...';
-- Deliberately ops-only/DB-direct, since granting cross-tenant reach through an API
-- endpoint would need its own careful authorization story that doesn't exist yet.
ALTER TABLE crm_setup."user" ADD COLUMN IF NOT EXISTS platform_admin BOOLEAN NOT NULL DEFAULT FALSE;
