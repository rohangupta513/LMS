package com.lms.backend.service;

import com.lms.backend.entity.LanCharge;
import com.lms.backend.entity.LoanAccount;
import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.enums.RepaymentStatus;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.repository.LanChargeRepository;
import com.lms.backend.repository.LoanAccountDueRepository;
import com.lms.backend.repository.LoanAccountRepository;
import com.lms.backend.repository.RepaymentSchedulerRepository;
import com.lms.backend.entity.LoanAccountDue;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactivationService {

  private final LoanAccountRepository loanAccountRepository;
  private final RepaymentSchedulerRepository repaymentSchedulerRepository;
  private final LanChargeRepository lanChargeRepository;
  private final LoanAccountDueRepository loanAccountDueRepository;
  
  @Lazy
  private final LoanApplicationService loanApplicationService;

  public LoanAccount activateAccount(Long lan) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.PENDING_CANCELLATION && account.getStatus() != LoanStatus.PENDING_FORECLOSURE) {
      throw new RuntimeException("Account must be in PENDING_CANCELLATION or PENDING_FORECLOSURE to be activated.");
    }

    LoanAccountDue ledger = loanAccountDueRepository.findById(lan).orElse(null);
    if (ledger != null && ledger.getTotalOutstandingAmount() <= 0) {
      throw new RuntimeException("Charges already paid");
    }

    LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
    if (lanCharge != null) {
        if (account.getStatus() == LoanStatus.PENDING_CANCELLATION) {
            lanCharge.setOtherFees(Math.max(0.0, lanCharge.getOtherFees() - 500.0));
        } else if (account.getStatus() == LoanStatus.PENDING_FORECLOSURE) {
            lanCharge.setOtherFees(Math.max(0.0, lanCharge.getOtherFees() - 1000.0));
            // Foreclosure aggregated everything into a new pending schedule. We must delete it.
            List<RepaymentScheduler> pendingSchedules = repaymentSchedulerRepository
                .findByLoanAccount_LanAndStatusOrderByDueDateAsc(lan, RepaymentStatus.PENDING);
            for (RepaymentScheduler r : pendingSchedules) {
              repaymentSchedulerRepository.delete(r);
            }
        }
        lanChargeRepository.save(lanCharge);
    }

    // 2. Restore all the old schedules back to PENDING!
    List<RepaymentScheduler> allSchedules = repaymentSchedulerRepository.findByLoanAccount_LanOrderByDueDateAsc(lan);
    for (RepaymentScheduler r : allSchedules) {
      // Restore cancelled schedules (Cancellation)
      if (r.getStatus() == RepaymentStatus.CANCELLED) {
        r.setStatus(RepaymentStatus.PENDING);
        repaymentSchedulerRepository.save(r);
      }
      // Restore forcefully paid schedules (Foreclosure) where totalDue > 0
      else if (r.getStatus() == RepaymentStatus.PAID && r.getTotalDue() > 0) {
        r.setStatus(RepaymentStatus.PENDING);
        repaymentSchedulerRepository.save(r);
      }
    }

    // 3. Mark the account back as ACTIVE!
    account.setStatus(LoanStatus.ACTIVE);
    loanAccountRepository.save(account);

    // 4. Force a recalculation of the master ledger so the UI numbers go back to normal
    return loanApplicationService.calculateDpdAndPenalties(lan, LocalDate.now());
  }
}
