package com.lms.backend.dto;

import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.RepaymentStatus;
import java.time.LocalDate;

public record RepaymentSchedulerResponse(
    Long rpsId,
    Long lan,
    LocalDate dueDate,
    Double totalPrincipalDue,
    Double totalInterestDue,
    Double totalDue,
    RepaymentStatus status
) {
  public static RepaymentSchedulerResponse fromEntity(RepaymentScheduler scheduler) {
    if (scheduler == null) return null;
    return new RepaymentSchedulerResponse(
        scheduler.getRpsId(),
        scheduler.getLan(),
        scheduler.getDueDate(),
        scheduler.getTotalPrincipalDue(),
        scheduler.getTotalInterestDue(),
        scheduler.getTotalDue(),
        scheduler.getStatus()
    );
  }
}
