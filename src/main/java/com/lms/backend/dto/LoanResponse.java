package com.lms.backend.dto;

import com.lms.backend.entity.Loan;
import com.lms.backend.enums.LoanType;

public record LoanResponse(
    Long loanId, 
    Long lenderId, 
    Double loanAmountMin, 
    Double loanAmountMax, 
    Double loanInterestMin, 
    Double loanInterestMax, 
    Integer loanTimeMin, 
    Integer loanTimeMax, 
    LoanType typeOfLoan
) {
  public static LoanResponse fromEntity(Loan loan) {
    if (loan == null) return null;
    return new LoanResponse(
        loan.getLoanId(), 
        loan.getLenderId(), 
        loan.getLoanAmountMin(), 
        loan.getLoanAmountMax(), 
        loan.getLoanInterestMin(), 
        loan.getLoanInterestMax(), 
        loan.getLoanTimeMin(), 
        loan.getLoanTimeMax(), 
        loan.getTypeOfLoan()
    );
  }
}
