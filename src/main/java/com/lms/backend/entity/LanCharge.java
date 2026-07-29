package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "lan_charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanCharge {

  @Id
  @Column(name = "lan", nullable = false)
  private Long lan;

  @Column(name = "dpd", nullable = false)
  private Integer dpd = 0;

  @Column(name = "penal_charges", nullable = false)
  private Double penalCharges = 0.0;

  @Column(name = "other_fees", nullable = false)
  private Double otherFees = 0.0;

  @Column(name = "last_calculated_date")
  private LocalDate lastCalculatedDate;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lan", referencedColumnName = "lan", insertable = false, updatable = false)
  private LoanAccount loanAccount;
}