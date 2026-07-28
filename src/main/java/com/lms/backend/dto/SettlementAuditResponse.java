package com.lms.backend.dto;

import com.lms.backend.entity.SettlementAudit;
import java.time.LocalDate;

public record SettlementAuditResponse(
    Long settId,
    Long rpsId,
    Long credId,
    Long lan,
    LocalDate dueDate,
    Double dueForThisMonth,
    Double dueFromPreviousMonths,
    Double chargesDue,
    Double totalDue,
    LocalDate dateOfCredit,
    Double amountDerived,
    Double principleDerived,
    Double interestDerived,
    Double chargesDerived,
    Boolean isSettled,
    String status
) {
  public static SettlementAuditResponse fromEntity(SettlementAudit audit) {
    if (audit == null) return null;
    return new SettlementAuditResponse(
        audit.getSettId(),
        audit.getRpsId(),
        audit.getCredId(),
        audit.getLan(),
        audit.getDueDate(),
        audit.getDueForThisMonth(),
        audit.getDueFromPreviousMonths(),
        audit.getChargesDue(),
        audit.getTotalDue(),
        audit.getDateOfCredit(),
        audit.getAmountDerived(),
        audit.getPrincipleDerived(),
        audit.getInterestDerived(),
        audit.getChargesDerived(),
        audit.getIsSettled(),
        audit.getStatus()
    );
  }
}
