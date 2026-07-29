package com.lms.backend.entity;

import com.lms.backend.enums.LoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Loan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long loanId;

  @NotNull(message = "Lender ID is required")
  @Column(name = "lender_id")
  private Long lenderId;

  @NotNull(message = "Minimum loan amount is required")
  @Min(value = 0, message = "Minimum loan amount must be non-negative")
  private Double loanAmountMin;
  
  @NotNull(message = "Maximum loan amount is required")
  @Min(value = 0, message = "Maximum loan amount must be non-negative")
  private Double loanAmountMax;
  
  @NotNull(message = "Minimum loan interest is required")
  @Min(value = 0, message = "Minimum loan interest must be non-negative")
  private Double loanInterestMin;
  
  @NotNull(message = "Maximum loan interest is required")
  @Min(value = 0, message = "Maximum loan interest must be non-negative")
  private Double loanInterestMax;
  
  @NotNull(message = "Minimum loan time is required")
  @Min(value = 0, message = "Minimum loan time must be non-negative")
  private Integer loanTimeMin;
  
  @NotNull(message = "Maximum loan time is required")
  @Min(value = 0, message = "Maximum loan time must be non-negative")
  private Integer loanTimeMax;

  @NotNull(message = "Type of loan is required")
  @Enumerated(EnumType.STRING)
  private LoanType typeOfLoan;
}
