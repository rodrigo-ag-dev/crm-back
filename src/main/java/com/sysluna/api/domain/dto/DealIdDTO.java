package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DealIdDTO {
  @NotBlank(message = "Deal ID is required")
  private String id;
}
