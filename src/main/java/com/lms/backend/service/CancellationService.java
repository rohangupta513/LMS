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
import com.lms.backend.repository.LoanCreditRepository;
import com.lms.backend.repository.RepaymentSchedulerRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancellationService {

  private final LoanAccountRepository loanAccountRepository;
  private final RepaymentSchedulerRepository repaymentSchedulerRepository;
  private final LanChargeRepository lanChargeRepository;
  private final LoanAccountDueRepository loanAccountDueRepository;
  private final LoanCreditRepository loanCreditRepository;

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  @Transactional
  public LoanAccount cancelLoan(Long lan, LocalDate dateOfCancellation) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new RuntimeException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.ACTIVE) {
      throw new RuntimeException("Only active loans can be cancelled.");
    }

    LocalDate actualDate = dateOfCancellation != null ? dateOfCancellation : LocalDate.now();
    long daysSinceStart = ChronoUnit.DAYS.between(account.getStartDate(), actualDate);
    if (daysSinceStart > 5) {
      throw new RuntimeException("Cancellation rejected: A loan can only be cancelled within 5 days of its start date.");
    }

    account.setStatus(LoanStatus.PENDING_CANCELLATION);

    // Halt scheduled dues
    List<RepaymentScheduler> dues =
        repaymentSchedulerRepository.findByLoanAccount_LanAndStatusOrderByDueDateAsc(
            lan, RepaymentStatus.PENDING);
    for (RepaymentScheduler r : dues) {
      r.setStatus(RepaymentStatus.CANCELLED);
      repaymentSchedulerRepository.save(r);
    }

    LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
    if (lanCharge == null) {
      lanCharge = new LanCharge();
      lanCharge.setLan(lan);
    }
    lanCharge.setDpd(0);
    lanCharge.setPenalCharges(0.0);

    // Assuming a flat cancellation fee of 500
    lanCharge.setOtherFees(500.0);
    lanChargeRepository.save(lanCharge);

    // Update Ledger to reflect cancellation state
    LoanAccountDue due =
        loanAccountDueRepository
            .findById(lan)
            .orElseThrow(() -> new RuntimeException("Loan Account Due not found"));

    due.setTotalOutstandingAmount(round(account.getAmount() + 500.0)); // Principal + Fee
    due.setTotalOutstandingPrinciple(account.getAmount());
    due.setTotalOutstandingInterest(0.0);
    due.setTotalChargesDue(500.0);
    due.setNextDueDate(LocalDate.now()); // Due immediately
    due.setNextDueAmount(round(account.getAmount() + 500.0));
    due.setNextDuePrinciple(account.getAmount());
    due.setNetDueInterest(0.0);
    due.setNextDueCharges(500.0);

    loanAccountDueRepository.save(due);
    return loanAccountRepository.save(account);
  }

  @Transactional
  public LoanAccount verifyCancellation(Long lan) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new RuntimeException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.PENDING_CANCELLATION) {
      throw new RuntimeException("Account is not pending cancellation.");
    }

    LoanAccountDue ledger = loanAccountDueRepository.findById(lan).orElseThrow();

    if (ledger.getTotalOutstandingAmount() > 0) {
      throw new RuntimeException("Cannot verify cancellation. Dues are not fully settled.");
    }

    List<com.lms.backend.entity.LoanCredit> credits = loanCreditRepository.findByLoanAccount_Lan(lan);
    for (com.lms.backend.entity.LoanCredit credit : credits) {
      if ("PENDING".equals(credit.getStatus()) || "PENDING_LENDER_VERIFICATION".equals(credit.getStatus())) {
        throw new RuntimeException("There are pending payments that need verification first.");
      }
    }

    account.setStatus(LoanStatus.CANCELLED);
    ledger.setIsSettled(true);
    loanAccountDueRepository.save(ledger);

    return loanAccountRepository.save(account);
  }
}
