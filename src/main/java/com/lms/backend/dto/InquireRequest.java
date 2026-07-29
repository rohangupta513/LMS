package com.lms.backend.dto;

import com.lms.backend.enums.LoanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class InquireRequest {
  @NotNull(message = "Amount is required")
  @Min(value = 0, message = "Amount must be non-negative")
  private Double amount;
  @NotNull(message = "Rate of interest is required")
  @Min(value = 0, message = "Rate of interest must be non-negative")
  private Double rateOfInterest;
  @NotNull(message = "Time period is required")
  @Min(value = 0, message = "Time period must be non-negative")
  private Integer timePeriod;
  @NotNull(message = "Type of loan is required")
  private LoanType typeOfLoan;
}
