package com.sysluna.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationResponse {
  private long expiresIn;
  private UserDTO user;
  private String token;
}

