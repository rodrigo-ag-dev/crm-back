package com.sysluna.api.config;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
public class OpenApiConfig {

  private static final String API_TITLE = "SYSLuna CRM API";
  private static final String API_VERSION_DEV = "1.0.0-dev";
  private static final String API_DESCRIPTION = "Custom CRM API documentation";
  private static final String SECURITY_SCHEME_KEY = "bearerAuth";
  private static final String CONTROLLER_PACKAGE = "com.sysluna.api.infrastructure.controller";
  private static final String AUTHENTICATION_TAG = "Authentication";
  private static final String HEALTHCHECK_TAG = "HealthCheck";

  private final Optional<BuildProperties> buildProperties;

  public OpenApiConfig(Optional<BuildProperties> buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(buildApiInfo())
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_KEY, buildBearerAuthScheme()));
  }

  @Bean
  public OpenApiCustomizer openApiCustomizer() {
    return openApi -> openApi.setTags(buildOrderedTags());
  }

  private Info buildApiInfo() {
    return new Info()
        .title(API_TITLE)
        .version(getApiVersion())
        .description(API_DESCRIPTION);
  }

  private SecurityScheme buildBearerAuthScheme() {
    return new SecurityScheme()
        .type(Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("JWT Bearer token");
  }

  private String getApiVersion() {
    return buildProperties
        .map(BuildProperties::getVersion)
        .orElse(API_VERSION_DEV);
  }

  private List<io.swagger.v3.oas.models.tags.Tag> buildOrderedTags() {
    Map<String, String> tagsMap = collectTagsFromControllers();
    return sortAndConvertToSwaggerTags(tagsMap);
  }

  private Map<String, String> collectTagsFromControllers() {
    var provider = createComponentProvider();
    Map<String, String> tagsMap = new LinkedHashMap<>();

    provider.findCandidateComponents(CONTROLLER_PACKAGE)
        .stream()
        .sorted(Comparator.comparing(bean -> bean.getBeanClassName()))
        .forEach(component -> extractTagIfPresent(component.getBeanClassName(), tagsMap));

    return tagsMap;
  }

  private void extractTagIfPresent(String className, Map<String, String> tagsMap) {
    try {
      Class<?> clazz = Class.forName(className);
      Tag tagAnnotation = clazz.getAnnotation(Tag.class);
      if (tagAnnotation != null) {
        tagsMap.put(tagAnnotation.name(), tagAnnotation.description());
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error loading class: " + className, e);
    }
  }

  private List<io.swagger.v3.oas.models.tags.Tag> sortAndConvertToSwaggerTags(Map<String, String> tagsMap) {
    return tagsMap.entrySet()
        .stream()
        .sorted(tagComparator())
        .map(entry -> new io.swagger.v3.oas.models.tags.Tag()
            .name(entry.getKey())
            .description(entry.getValue()))
        .collect(Collectors.toList());
  }

  private Comparator<Map.Entry<String, String>> tagComparator() {
    return (a, b) -> {
      if (a.getKey().equals(HEALTHCHECK_TAG))
        return -1;
      if (b.getKey().equals(HEALTHCHECK_TAG))
        return 1;
      if (a.getKey().equals(AUTHENTICATION_TAG))
        return -1;
      if (b.getKey().equals(AUTHENTICATION_TAG))
        return 1;
      return a.getKey().compareTo(b.getKey());
    };
  }

  private ClassPathScanningCandidateComponentProvider createComponentProvider() {
    var provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
    return provider;
  }
}
