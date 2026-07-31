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

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double nextDueAmount;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double nextDuePrinciple;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double netDueInterest;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double nextDueCharges;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalOutstandingAmount;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalOutstandingPrinciple;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalOutstandingInterest;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalDerivedAmount;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalDerivedPrinciple;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalDerivedInterest;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalChargesDue;

  @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.lms.backend.utils.DoubleRoundingSerializer.class)
  private Double totalChargesDerived;

  private Boolean isSettled;
  private Boolean isCancelled;
  private Boolean isForeclosed;
}
