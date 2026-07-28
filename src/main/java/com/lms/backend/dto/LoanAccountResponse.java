package com.lms.backend.dto;

import com.lms.backend.entity.LoanAccount;
import com.lms.backend.enums.LoanStatus;
import java.time.LocalDate;

public record LoanAccountResponse(
    Long lan,
    Long userId,
    Long lenderId,
    Long loanId,
    Double rateOfInterest,
    Double amount,
    Integer timePeriod,
    LocalDate startDate,
    LoanStatus status
) {
  public static LoanAccountResponse fromEntity(LoanAccount account) {
    if (account == null) return null;
    return new LoanAccountResponse(
        account.getLan(),
        account.getUserId(),
        account.getLenderId(),
        account.getLoanId(),
        account.getRateOfInterest(),
        account.getAmount(),
        account.getTimePeriod(),
        account.getStartDate(),
        account.getStatus()
    );
  }
}
