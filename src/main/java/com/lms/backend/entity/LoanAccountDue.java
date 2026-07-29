package com.lms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "loan_account_dues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LoanAccountDue {
  @Id private Long lan;

  @OneToOne
  @MapsId
  @JoinColumn(name = "lan")
  @JsonIgnore
  private LoanAccount loanAccount;

  private LocalDate nextDueDate;
  private Double nextDueAmount;
  private Double nextDuePrinciple;
  private Double netDueInterest;
  private Double nextDueCharges;
  private Double totalOutstandingAmount;
  private Double totalOutstandingPrinciple;
  private Double totalOutstandingInterest;
  private Double totalDerivedAmount;
  private Double totalDerivedPrinciple;
  private Double totalDerivedInterest;
  private Double totalChargesDue;
  private Double totalChargesDerived;
  private Boolean isSettled;
  private Boolean isCancelled;
  private Boolean isForeclosed;
}
