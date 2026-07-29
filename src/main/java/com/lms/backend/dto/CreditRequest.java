package com.lms.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) for CreditRequest.
 * Used to safely transfer data between the client and server layers.
 */
public class CreditRequest {
  @NotNull(message = "LAN is required")
  private Long lan;
  @NotNull(message = "Amount is required")
  @Min(value = 0, message = "Amount must be non-negative")
  private Double amount;
  private java.time.LocalDate dateOfCredit;
}
