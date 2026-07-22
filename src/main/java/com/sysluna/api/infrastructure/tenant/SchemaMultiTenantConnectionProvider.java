package com.sysluna.api.infrastructure.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

/**
 * Schema-based multi-tenancy for Hibernate: every connection Hibernate acquires for a
 * given tenant identifier gets `SET search_path` pointed at that tenant's Postgres
 * schema, and reset to "public" before the connection returns to the pool. Resetting on
 * release is essential - Hikari reuses physical connections across requests, and
 * without the reset the next request (a different tenant) would inherit the previous
 * tenant's search_path.
 */
@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

  private static final String PUBLIC_SCHEMA = "public";

  private final transient DataSource dataSource;

  public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Connection getAnyConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public void releaseAnyConnection(Connection connection) throws SQLException {
    connection.close();
  }

  @Override
  public Connection getConnection(String tenantIdentifier) throws SQLException {
    Connection connection = getAnyConnection();
    setSearchPath(connection, tenantIdentifier);
    return connection;
  }

  @Override
  public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
    try {
      setSearchPath(connection, PUBLIC_SCHEMA);
    } finally {
      connection.close();
    }
  }

  @Override
  public boolean supportsAggressiveRelease() {
    return false;
  }

  @Override
  public boolean isUnwrappableAs(Class<?> unwrapType) {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> unwrapType) {
    throw new UnsupportedOperationException("SchemaMultiTenantConnectionProvider does not support unwrapping");
  }

  private void setSearchPath(Connection connection, String schemaName) throws SQLException {
    if (!PUBLIC_SCHEMA.equals(schemaName) && !TenantSchemaNames.isValid(schemaName)) {
      throw new SQLException("Refusing to switch to an invalid tenant schema name: " + schemaName);
    }
    try (Statement statement = connection.createStatement()) {
      statement.execute("SET search_path TO \"" + schemaName + "\", public");
    }
  }
}
