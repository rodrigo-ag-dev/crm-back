package com.sysluna.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System health status")
public record HealthCheckResponse(
    @Schema(description = "System status", example = "UP") String status,
    @Schema(description = "Whether no users exist yet and the initial admin setup must run", example = "false") boolean setupRequired) {
}
