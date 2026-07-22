package com.sysluna.api.infrastructure.tenant;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

/**
 * Runs the per-tenant migration set (db/migration/tenant) against a single tenant's
 * Postgres schema. Separate from the app's main, auto-configured Flyway instance (which
 * only manages the shared "public" schema - see db/migration/global) since each tenant
 * schema needs its own migration run and its own schema-history table. Uses the plain
 * DataSource directly (not through Hibernate's tenant-aware connection provider) since
 * Flyway manages its own JDBC connections independently.
 */
@Component
public class TenantFlywayMigrator {

  private static final String TENANT_MIGRATIONS_LOCATION = "classpath:db/migration/tenant";
  private static final String TENANT_HISTORY_TABLE = "flyway_tenant_history";

  private final DataSource dataSource;

  public TenantFlywayMigrator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void migrate(String schemaName) {
    if (!TenantSchemaNames.isValid(schemaName)) {
      throw new IllegalArgumentException("Refusing to migrate an invalid tenant schema name: " + schemaName);
    }

    Flyway.configure()
        .dataSource(dataSource)
        .schemas(schemaName)
        .table(TENANT_HISTORY_TABLE)
        .locations(TENANT_MIGRATIONS_LOCATION)
        .baselineOnMigrate(true)
        .load()
        .migrate();
  }
}
