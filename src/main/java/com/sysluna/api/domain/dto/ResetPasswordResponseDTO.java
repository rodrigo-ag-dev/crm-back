package com.sysluna.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordResponseDTO {
  private String temporaryPassword;
}
