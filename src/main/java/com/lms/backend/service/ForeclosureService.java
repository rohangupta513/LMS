package com.lms.backend.service;

import com.lms.backend.entity.LanCharge;
import com.lms.backend.entity.LoanAccount;
import com.lms.backend.entity.LoanAccountDue;
import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.enums.RepaymentStatus;
import com.lms.backend.repository.LanChargeRepository;
import com.lms.backend.repository.LoanAccountDueRepository;
import com.lms.backend.repository.LoanAccountRepository;
import com.lms.backend.repository.RepaymentSchedulerRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForeclosureService {

  private final LoanAccountRepository loanAccountRepository;
  private final RepaymentSchedulerRepository repaymentSchedulerRepository;
  private final LanChargeRepository lanChargeRepository;
  private final LoanAccountDueRepository loanAccountDueRepository;

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  @Transactional
  public LoanAccount forecloseLoan(Long lan) {
    LoanAccount account =
        loanAccountRepository
            .findById(lan)
            .orElseThrow(() -> new RuntimeException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.ACTIVE) {
      throw new RuntimeException("Foreclosure can only be applied to Active loan accounts.");
    }

    double FORECLOSURE_CHARGE = 1000.0;
    account.setStatus(LoanStatus.PENDING_FORECLOSURE);
    loanAccountRepository.save(account);

    List<RepaymentScheduler> dues =
        repaymentSchedulerRepository.findByLoanAccount_LanAndStatusOrderByDueDateAsc(
            lan, RepaymentStatus.PENDING);

    double totalPDue = 0, totalIDue = 0;
    for (RepaymentScheduler r : dues) {
      totalPDue += r.getTotalPrincipalDue();
      totalIDue += r.getTotalInterestDue();
      r.setStatus(RepaymentStatus.PAID); // mark existing as paid
      repaymentSchedulerRepository.save(r);
    }

    totalPDue = round(totalPDue);
    totalIDue = round(totalIDue);
    double totalDue = round(totalPDue + totalIDue);

    RepaymentScheduler forecloseDue =
        RepaymentScheduler.builder()
            .loanAccount(account)
            .dueDate(LocalDate.now())
            .totalPrincipalDue(totalPDue)
            .totalInterestDue(totalIDue)
            .totalDue(totalDue)
            .status(RepaymentStatus.PENDING)
            .build();
    repaymentSchedulerRepository.save(forecloseDue);

    // Add Foreclosure Fee to LanCharge
    LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
    if (lanCharge == null) {
      lanCharge = new LanCharge();
      lanCharge.setLan(lan);
      lanCharge.setDpd(0);
      lanCharge.setPenalCharges(0.0);
      lanCharge.setOtherFees(0.0);
      lanCharge.setLastCalculatedDate(LocalDate.now());
    }
    lanCharge.setOtherFees(lanCharge.getOtherFees() + FORECLOSURE_CHARGE);
    lanChargeRepository.save(lanCharge);

    LoanAccountDue due = loanAccountDueRepository.findById(lan).orElse(null);
    if (due != null) {
      // Defer setIsForeclosed(true) until verification
      due.setNextDueDate(LocalDate.now());
      due.setNextDueAmount(forecloseDue.getTotalDue());
      due.setNextDuePrinciple(forecloseDue.getTotalPrincipalDue());
      due.setNetDueInterest(forecloseDue.getTotalInterestDue());
      due.setNextDueCharges(lanCharge.getPenalCharges() + lanCharge.getOtherFees());

      due.setTotalOutstandingAmount(forecloseDue.getTotalDue() + lanCharge.getPenalCharges() + lanCharge.getOtherFees());
      due.setTotalOutstandingPrinciple(forecloseDue.getTotalPrincipalDue());
      due.setTotalOutstandingInterest(forecloseDue.getTotalInterestDue());
      due.setTotalChargesDue(lanCharge.getPenalCharges() + lanCharge.getOtherFees());
      loanAccountDueRepository.save(due);
    }

    return account;
  }

  @Transactional
  public LoanAccount verifyForeclosure(Long lan) {
    LoanAccount account =
        loanAccountRepository
            .findById(lan)
            .orElseThrow(() -> new RuntimeException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.PENDING_FORECLOSURE) {
      throw new RuntimeException("Loan is not in PENDING_FORECLOSURE status.");
    }

    LoanAccountDue ledger = loanAccountDueRepository.findById(lan).orElseThrow();
    if (!ledger.getIsSettled()) {
      throw new RuntimeException("Foreclosure payment has not been fully settled yet. Please pay the remaining balance.");
    }

    account.setStatus(LoanStatus.FORECLOSED);
    ledger.setIsForeclosed(true);
    loanAccountDueRepository.save(ledger);

    return loanAccountRepository.save(account);
  }
}
