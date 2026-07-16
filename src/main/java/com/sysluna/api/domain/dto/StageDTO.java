package com.sysluna.api.domain.dto;

import com.sysluna.api.domain.model.Stage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StageDTO extends BaseDTO {
  private String id;

  @NotBlank(message = "Name is required")
  @Size(max = 255, message = "Name must be at most 255 characters")
  private String name;

  private String description;

  @Size(max = 36, message = "Color must be at most 36 characters")
  private String color;

  @Min(value = 0, message = "Order cannot be negative")
  private int order;

  @Min(value = 0, message = "SLA days cannot be negative")
  private int daysSla;

  public static StageDTO fromStage(Stage stage) {
    return StageDTO.builder()
        .id(stage.getId())
        .name(stage.getName())
        .description(stage.getDescription())
        .color(stage.getColor())
        .order(stage.getOrder())
        .createdAt(stage.getCreatedAt())
        .updatedAt(stage.getUpdatedAt())
        .daysSla(stage.getDaysSla())
        .build();
  }
}
