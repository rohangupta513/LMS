package com.lms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "loan_credits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the LoanCredit database table.
 * Contains the data model and structural mappings.
 */
public class LoanCredit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long credId;

  @ManyToOne
  @JoinColumn(name = "lan")
  @JsonIgnore
  private LoanAccount loanAccount;

  @JsonProperty("lan")
  public Long getLan() { return loanAccount != null ? loanAccount.getLan() : null; }

  private LocalDate dateOfCredit;
  private Double amtCredited;
  private Double totalPrincipleDerived;
  private Double totalInterestDerived;
  private Double totalChargesDerived;
  private String status;
}
