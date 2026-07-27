package com.lms.backend.entity;

import com.lms.backend.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the Loan database table.
 * Contains the data model and structural mappings.
 */
public class Loan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long loanId;

  @Column(name = "lender_id")
  private Long lenderId;

  private Double loanAmountMin;
  private Double loanAmountMax;
  private Double loanInterestMin;
  private Double loanInterestMax;
  private Integer loanTimeMin;
  private Integer loanTimeMax;

  @Enumerated(EnumType.STRING)
  private LoanType typeOfLoan;
}
