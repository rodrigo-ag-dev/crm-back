package com.sysluna.api.domain.dto;

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
public class NameDTO extends BaseDTO {
  private String id;
  private String name;

  public static NameDTO fromDeal(String id, String name) {
    return NameDTO.builder()
        .id(id)
        .name(name)
        .build();
  }
}
