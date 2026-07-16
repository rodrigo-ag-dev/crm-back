package com.sysluna.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DealStageDTO {
  @NotBlank(message = "Deal ID is required")
  private String id;

  @NotBlank(message = "Stage ID is required")
  private String stageId;
}
