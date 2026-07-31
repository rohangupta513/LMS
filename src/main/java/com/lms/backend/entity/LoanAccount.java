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
  
  @JsonProperty("userId")
  public void setUserId(Long userId) {
      if (this.user == null) this.user = new User();
      this.user.setUserId(userId);
  }

  @ManyToOne
  @JoinColumn(name = "lender_id")
  @JsonIgnore
  private Lender lender;

  @JsonProperty("lenderId")
  public Long getLenderId() { return lender != null ? lender.getLenderId() : null; }
  
  @JsonProperty("lenderId")
  public void setLenderId(Long lenderId) {
      if (this.lender == null) this.lender = new Lender();
      this.lender.setLenderId(lenderId);
  }

  @ManyToOne
  @JoinColumn(name = "loan_id")
  @JsonIgnore
  private Loan loan;

  @JsonProperty("loanId")
  public Long getLoanId() { return loan != null ? loan.getLoanId() : null; }
  
  @JsonProperty("loanId")
  public void setLoanId(Long loanId) {
      if (this.loan == null) this.loan = new Loan();
      this.loan.setLoanId(loanId);
  }

  private Double rateOfInterest;
  private Double amount;
  private Integer timePeriod;
  private java.time.LocalDate startDate;

  @Enumerated(EnumType.STRING)
  private LoanStatus status;
}
