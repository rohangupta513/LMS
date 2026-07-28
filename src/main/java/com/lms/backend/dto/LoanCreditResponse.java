package com.lms.backend.dto;

import com.lms.backend.entity.LoanCredit;
import java.time.LocalDate;

public record LoanCreditResponse(
    Long credId,
    Long lan,
    LocalDate dateOfCredit,
    Double amtCredited,
    Double totalPrincipleDerived,
    Double totalInterestDerived,
    Double totalChargesDerived,
    String status
) {
  public static LoanCreditResponse fromEntity(LoanCredit credit) {
    if (credit == null) return null;
    return new LoanCreditResponse(
        credit.getCredId(),
        credit.getLan(),
        credit.getDateOfCredit(),
        credit.getAmtCredited(),
        credit.getTotalPrincipleDerived(),
        credit.getTotalInterestDerived(),
        credit.getTotalChargesDerived(),
        credit.getStatus()
    );
  }
}
