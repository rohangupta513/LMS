package com.lms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lms.backend.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the LoanAccount database table.
 * Contains the data model and structural mappings.
 */
public class LoanAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long lan;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @JsonProperty("userId")
  public Long getUserId() { return user != null ? user.getUserId() : null; }

  @ManyToOne
  @JoinColumn(name = "lender_id")
  @JsonIgnore
  private Lender lender;

  @JsonProperty("lenderId")
  public Long getLenderId() { return lender != null ? lender.getLenderId() : null; }

  @ManyToOne
  @JoinColumn(name = "loan_id")
  @JsonIgnore
  private Loan loan;

  @JsonProperty("loanId")
  public Long getLoanId() { return loan != null ? loan.getLoanId() : null; }

  private Double rateOfInterest;
  private Double amount;
  private Integer timePeriod;
  private java.time.LocalDate startDate;

  @Enumerated(EnumType.STRING)
  private LoanStatus status;
}
