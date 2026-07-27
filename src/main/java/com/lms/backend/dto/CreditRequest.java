package com.lms.backend.dto;

import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) for CreditRequest.
 * Used to safely transfer data between the client and server layers.
 */
public class CreditRequest {
  private Long lan;
  private Double amount;
  private java.time.LocalDate dateOfCredit;
}
