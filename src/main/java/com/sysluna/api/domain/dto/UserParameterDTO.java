package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserParameterDTO {
  @NotBlank(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Parameter ID is required")
  private String parameterId;

  private String value;
}
