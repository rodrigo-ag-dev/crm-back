package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserActiveDTO {
  @NotNull(message = "Active is required")
  private Boolean active;
}
