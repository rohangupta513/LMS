package com.lms.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class ApplyRequest {
  @NotNull(message = "User ID is required")
  private Long userId;
  @NotNull(message = "Lender ID is required")
  private Long lenderId;
  @NotNull(message = "Loan ID is required")
  private Long loanId;
  @NotNull(message = "Amount is required")
  @Min(value = 0, message = "Amount must be non-negative")
  private Double amount;
  @NotNull(message = "Rate of interest is required")
  @Min(value = 0, message = "Rate of interest must be non-negative")
  private Double rateOfInterest;
  @NotNull(message = "Time period is required")
  @Min(value = 0, message = "Time period must be non-negative")
  private Integer timePeriod;
  private java.time.LocalDate applicationDate;
}
