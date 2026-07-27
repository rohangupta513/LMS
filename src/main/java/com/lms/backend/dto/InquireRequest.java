package com.lms.backend.dto;

import com.lms.backend.enums.LoanType;
import lombok.Data;

@Data
/**
 * Data Transfer Object (DTO) for InquireRequest.
 * Used to safely transfer data between the client and server layers.
 */
public class InquireRequest {
  private Double amount;
  private Double rateOfInterest;
  private Integer timePeriod;
  private LoanType typeOfLoan;
}
