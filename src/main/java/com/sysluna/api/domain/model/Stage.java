package com.sysluna.api.domain.model;

import com.sysluna.api.domain.dto.StageDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stage")
public class Stage extends BaseModel {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private String color;

  @Column(name = "\"order\"", nullable = false)
  private int order;

  @Column(nullable = false)
  private int daysSla;

  public static Stage fromDTO(StageDTO dto) {
    return Stage.builder()
        .name(dto.getName())
        .description(dto.getDescription())
        .color(dto.getColor())
        .order(dto.getOrder())
        .daysSla(dto.getDaysSla())
        .build();
  }
}
