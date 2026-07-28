package com.lms.backend.dto;

import com.lms.backend.entity.LoanAccountDue;
import java.time.LocalDate;

public record LoanAccountDueResponse(
    Long lan,
    LocalDate nextDueDate,
    Double nextDueAmount,
    Double nextDuePrinciple,
    Double netDueInterest,
    Double nextDueCharges,
    Double totalOutstandingAmount,
    Double totalOutstandingPrinciple,
    Double totalOutstandingInterest,
    Double totalDerivedAmount,
    Double totalDerivedPrinciple,
    Double totalDerivedInterest,
    Double totalChargesDue,
    Double totalChargesDerived,
    Boolean isSettled,
    Boolean isCancelled,
    Boolean isForeclosed
) {
  public static LoanAccountDueResponse fromEntity(LoanAccountDue due) {
    if (due == null) return null;
    return new LoanAccountDueResponse(
        due.getLan(),
        due.getNextDueDate(),
        due.getNextDueAmount(),
        due.getNextDuePrinciple(),
        due.getNetDueInterest(),
        due.getNextDueCharges(),
        due.getTotalOutstandingAmount(),
        due.getTotalOutstandingPrinciple(),
        due.getTotalOutstandingInterest(),
        due.getTotalDerivedAmount(),
        due.getTotalDerivedPrinciple(),
        due.getTotalDerivedInterest(),
        due.getTotalChargesDue(),
        due.getTotalChargesDerived(),
        due.getIsSettled(),
        due.getIsCancelled(),
        due.getIsForeclosed()
    );
  }
}
