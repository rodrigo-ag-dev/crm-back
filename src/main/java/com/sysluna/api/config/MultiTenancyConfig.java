package com.sysluna.api.config;

import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sysluna.api.infrastructure.tenant.SchemaMultiTenantConnectionProvider;
import com.sysluna.api.infrastructure.tenant.TenantIdentifierResolver;

@Configuration
public class MultiTenancyConfig {

  @Bean
  public HibernatePropertiesCustomizer hibernateTenancyCustomizer(
      SchemaMultiTenantConnectionProvider connectionProvider,
      TenantIdentifierResolver tenantIdentifierResolver) {
    return properties -> {
      properties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
      properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
    };
  }
}
