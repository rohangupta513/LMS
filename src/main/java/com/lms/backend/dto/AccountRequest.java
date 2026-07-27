package com.lms.backend.dto;

import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) for AccountRequest.
 * Used to safely transfer data between the client and server layers.
 */
public class AccountRequest {
  private Long lan;
}
