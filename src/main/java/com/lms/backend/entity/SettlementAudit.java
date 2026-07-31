package com.lms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "settlement_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SettlementAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long settId;

  @ManyToOne
  @JoinColumn(name = "rps_id")
  @JsonIgnore
  private RepaymentScheduler repaymentScheduler;

  @JsonProperty("rpsId")
  public Long getRpsId() { return repaymentScheduler != null ? repaymentScheduler.getRpsId() : null; }
  
  @JsonProperty("rpsId")
  public void setRpsId(Long rpsId) {
      if (this.repaymentScheduler == null) this.repaymentScheduler = new RepaymentScheduler();
      this.repaymentScheduler.setRpsId(rpsId);
  }

  @ManyToOne
  @JoinColumn(name = "cred_id")
  @JsonIgnore
  private LoanCredit loanCredit;

  @JsonProperty("credId")
  public Long getCredId() { return loanCredit != null ? loanCredit.getCredId() : null; }
  
  @JsonProperty("credId")
  public void setCredId(Long credId) {
      if (this.loanCredit == null) this.loanCredit = new LoanCredit();
      this.loanCredit.setCredId(credId);
  }

  @ManyToOne
  @JoinColumn(name = "lan")
  @JsonIgnore
  private LoanAccount loanAccount;

  @JsonProperty("lan")
  public Long getLan() { return loanAccount != null ? loanAccount.getLan() : null; }
  
  @JsonProperty("lan")
  public void setLan(Long lan) {
      if (this.loanAccount == null) this.loanAccount = new LoanAccount();
      this.loanAccount.setLan(lan);
  }

  private LocalDate dueDate;
  private Double dueForThisMonth;
  private Double dueFromPreviousMonths;
  private Double chargesDue;
  private Double totalDue;
  private LocalDate dateOfCredit;
  private Double amountDerived;
  private Double principleDerived;
  private Double interestDerived;
  private Double chargesDerived;
  private Boolean isSettled;
  private String status;
}
