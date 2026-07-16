package com.sysluna.api.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DealStaleDTO {
  private String id;
  private String title;
  private String companyName;
  private LocalDateTime updatedAt;
}
