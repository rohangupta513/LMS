package com.lms.backend.dto;

import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) for ApplyRequest.
 * Used to safely transfer data between the client and server layers.
 */
public class ApplyRequest {
  private Long userId;
  private Long lenderId;
  private Long loanId;
  private Double amount;
  private Double rateOfInterest;
  private Integer timePeriod;
  private java.time.LocalDate applicationDate;
}
