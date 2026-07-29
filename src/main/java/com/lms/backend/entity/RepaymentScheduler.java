package com.lms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lms.backend.enums.RepaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "repayment_schedulers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RepaymentScheduler {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long rpsId;

  @ManyToOne
  @JoinColumn(name = "lan")
  @JsonIgnore
  private LoanAccount loanAccount;

  @JsonProperty("lan")
  public Long getLan() { return loanAccount != null ? loanAccount.getLan() : null; }

  private LocalDate dueDate;

  @Column(name = "total_principal_due", nullable = false)
  private Double totalPrincipalDue = 0.0;

  @Column(name = "total_interest_due", nullable = false)
  private Double totalInterestDue = 0.0;

  @Column(name = "total_due", nullable = false)
  private Double totalDue = 0.0;

  @Enumerated(EnumType.STRING)
  private RepaymentStatus status;
}
