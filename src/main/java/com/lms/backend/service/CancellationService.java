package com.lms.backend.service;

import com.lms.backend.entity.LanCharge;
import com.lms.backend.entity.LoanAccount;
import com.lms.backend.entity.LoanAccountDue;
import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.enums.RepaymentStatus;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.repository.LanChargeRepository;
import com.lms.backend.repository.LoanAccountDueRepository;
import com.lms.backend.repository.LoanAccountRepository;
import com.lms.backend.repository.LoanCreditRepository;
import com.lms.backend.repository.RepaymentSchedulerRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.CacheManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancellationService {

  private final LoanAccountRepository loanAccountRepository;
  private final RepaymentSchedulerRepository repaymentSchedulerRepository;
  private final LanChargeRepository lanChargeRepository;
  private final LoanAccountDueRepository loanAccountDueRepository;
  private final LoanCreditRepository loanCreditRepository;
  private final CacheManager cacheManager;

  private double round(double value) {
    return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
  }

  @Transactional
  public LoanAccount cancelLoan(Long lan, LocalDate dateOfCancellation) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

    log.info("Initiating cancellation for LAN: {}. Current status: {}", lan, account.getStatus());

    if (account.getStatus() != LoanStatus.ACTIVE) {
      log.error("Cancellation failed for LAN: {}. Account is not ACTIVE.", lan);
      throw new RuntimeException("Only active loans can be cancelled.");
    }

    LocalDate actualDate = dateOfCancellation != null ? dateOfCancellation : LocalDate.now();
    long daysSinceStart = ChronoUnit.DAYS.between(account.getStartDate(), actualDate);
    if (daysSinceStart > 5) {
      throw new RuntimeException("Cancellation rejected: A loan can only be cancelled within 5 days of its start date.");
    }

    account.setStatus(LoanStatus.PENDING_CANCELLATION) ;

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
            .orElseThrow(() -> new ResourceNotFoundException("Loan Account Due not found"));

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
    log.info("Successfully applied cancellation charges and marked LAN: {} as PENDING_CANCELLATION", lan);
    
    if (cacheManager != null) {
        if (cacheManager.getCache("loanAccount") != null) cacheManager.getCache("loanAccount").evict(lan);
        if (cacheManager.getCache("loanAccounts") != null) cacheManager.getCache("loanAccounts").clear();
        if (cacheManager.getCache("loanDues") != null) cacheManager.getCache("loanDues").evict(lan);
        if (cacheManager.getCache("lanCharges") != null) cacheManager.getCache("lanCharges").evict(lan);
        if (cacheManager.getCache("repaymentSchedules") != null) cacheManager.getCache("repaymentSchedules").evict(lan);
        if (cacheManager.getCache("allRepaymentSchedules") != null) cacheManager.getCache("allRepaymentSchedules").clear();
    }
    
    return loanAccountRepository.save(account);
  }

  @Transactional
  public LoanAccount verifyCancellation(Long lan) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

    log.info("Verifying cancellation for LAN: {}. Current status: {}", lan, account.getStatus());

    if (account.getStatus() != LoanStatus.PENDING_CANCELLATION) {
      log.error("Cancellation verification failed for LAN: {}. Account is not PENDING_CANCELLATION.", lan);
      throw new RuntimeException("Account is not pending cancellation.");
    }

    LoanAccountDue ledger = loanAccountDueRepository.findById(lan).orElseThrow();

    if (ledger.getTotalOutstandingAmount() > 0 || ledger.getTotalChargesDue() > 0) {
      throw new RuntimeException("Cannot verify cancellation. Dues are not fully settled.");
    }

    account.setStatus(LoanStatus.CANCELLED);
    ledger.setIsSettled(true);
    loanAccountDueRepository.save(ledger);

    loanAccountDueRepository.save(ledger);

    log.info("Successfully verified cancellation for LAN: {}. Account is now CANCELLED.", lan);
    
    if (cacheManager != null) {
        if (cacheManager.getCache("loanAccount") != null) cacheManager.getCache("loanAccount").evict(lan);
        if (cacheManager.getCache("loanAccounts") != null) cacheManager.getCache("loanAccounts").clear();
        if (cacheManager.getCache("loanDues") != null) cacheManager.getCache("loanDues").evict(lan);
        if (cacheManager.getCache("lanCharges") != null) cacheManager.getCache("lanCharges").evict(lan);
    }
    
    return loanAccountRepository.save(account);
  }
}
