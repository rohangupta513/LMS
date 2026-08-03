package com.lms.backend.service;

import com.lms.backend.dto.CreditRequest;
import com.lms.backend.entity.*;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.enums.RepaymentStatus;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SettlementService {

  @Autowired private LoanAccountRepository loanAccountRepository;
  @Autowired private LoanAccountDueRepository loanAccountDueRepository;
  @Autowired private RepaymentSchedulerRepository repaymentSchedulerRepository;
  @Autowired private LoanCreditRepository loanCreditRepository;
  @Autowired private SettlementAuditRepository settlementAuditRepository;
  @Autowired private LanChargeRepository lanChargeRepository;
  @Autowired private LoanApplicationService loanApplicationService;
  @Autowired private CacheManager cacheManager;

  //FIFO and PIC
  @Transactional
  public LoanCredit processCredit(CreditRequest request) {
    log.info("Processing credit request for LAN: {}, amount: {}", request.getLan(), request.getAmount());
    LoanAccount account =
        loanAccountRepository
            .findById(request.getLan())
            .orElseThrow(() -> new ResourceNotFoundException("Loan account not found"));

    LocalDate dateOfCredit = request.getDateOfCredit() != null ? request.getDateOfCredit() : LocalDate.now();

    if (account.getStatus() != LoanStatus.PENDING_CANCELLATION && account.getStatus() != LoanStatus.PENDING_FORECLOSURE) {
      loanApplicationService.calculateDpdAndPenalties(account.getLan(), dateOfCredit);
    }

    List<RepaymentScheduler> dues =
        repaymentSchedulerRepository.findByLoanAccount_LanAndStatusOrderByDueDateAsc(
            request.getLan(), RepaymentStatus.PENDING);

    if (!dues.isEmpty()) {
      if (dateOfCredit.isBefore(dues.get(0).getDueDate())) {
        throw new RuntimeException("user must pay on or after the due date");
      }
    }

    LanCharge lanCharge = lanChargeRepository.findById(account.getLan()).orElse(null);
    double globalPenalCharges = lanCharge != null ? lanCharge.getPenalCharges() : 0.0;
    double globalOtherFees = lanCharge != null ? lanCharge.getOtherFees() : 0.0;
    double globalChargesDue = globalPenalCharges + globalOtherFees;

    double maxAllowedPayment = globalChargesDue;
    for (RepaymentScheduler due : dues) {
      if (account.getStatus() == LoanStatus.PENDING_FORECLOSURE ||
          account.getStatus() == LoanStatus.PENDING_CANCELLATION ||
          !due.getDueDate().isAfter(dateOfCredit)) {
        maxAllowedPayment += due.getTotalDue();
      }
    }

    if (request.getAmount() > round(maxAllowedPayment)) {
      if (account.getStatus() == LoanStatus.PENDING_FORECLOSURE || account.getStatus() == LoanStatus.PENDING_CANCELLATION) {
        throw new RuntimeException("Payment exceeds total outstanding amount.");
      } else {
        throw new RuntimeException("Payment exceeds current dues. To pay off the entire loan, please request a Foreclosure.");
      }
    }

    LoanCredit credit =
        LoanCredit.builder()
            .loanAccount(account)
            .dateOfCredit(dateOfCredit)
            .amtCredited(request.getAmount())
            .totalPrincipleDerived(0.0)
            .totalInterestDerived(0.0)
            .totalChargesDerived(0.0)
            .status("PENDING")
            .build();

    credit.setStatus("PENDING_LENDER_VERIFICATION");
    credit = loanCreditRepository.save(credit);
    
    if (cacheManager != null) {
        if (cacheManager.getCache("loanCredits") != null) cacheManager.getCache("loanCredits").evict(request.getLan());
        if (cacheManager.getCache("allLoanCredits") != null) cacheManager.getCache("allLoanCredits").clear();
    }
    
    return credit;
  }

  @Transactional
  public LoanCredit verifyCredit(Long credId) {
    log.info("Verifying credit ID: {}", credId);
    LoanCredit credit =
        loanCreditRepository
            .findById(credId)
            .orElseThrow(() -> new ResourceNotFoundException("Credit request not found"));

    if (!"PENDING_LENDER_VERIFICATION".equals(credit.getStatus())) {
      throw new RuntimeException("Credit is already verified or in invalid state.");
    }

        LoanAccount account = credit.getLoanAccount();
    java.time.LocalDate dateOfCredit = credit.getDateOfCredit();

    java.util.List<RepaymentScheduler> dues =
        repaymentSchedulerRepository.findByLoanAccount_LanAndStatusOrderByDueDateAsc(
            account.getLan(), RepaymentStatus.PENDING);

    LanCharge lanCharge = lanChargeRepository.findById(account.getLan()).orElse(null);
    double globalPenalCharges = lanCharge != null ? lanCharge.getPenalCharges() : 0.0;
    double globalOtherFees = lanCharge != null ? lanCharge.getOtherFees() : 0.0;
    double globalChargesDue = globalPenalCharges + globalOtherFees;

double remainingAmount = credit.getAmtCredited();

    double totalPrinDerived = 0;
    double totalIntDerived = 0;
    double totalCharDerived = 0;

    // Calculate global loan totals before payment distribution
    double globalDueForThisMonth = 0.0;
    double globalDueFromPreviousMonths = 0.0;
    boolean foundCurrentMonth = false;

    for (RepaymentScheduler d : dues) {
      if (d.getDueDate().isBefore(dateOfCredit)) {
        globalDueFromPreviousMonths += (d.getTotalPrincipalDue() + d.getTotalInterestDue());
      } else {
        if (!foundCurrentMonth) {
          globalDueForThisMonth += (d.getTotalPrincipalDue() + d.getTotalInterestDue());
          foundCurrentMonth = true;
        }
      }
    }
    double globalTotalDue = globalDueForThisMonth + globalDueFromPreviousMonths + globalChargesDue;

    boolean globalChargesPaid = false;

    for (RepaymentScheduler due : dues) {
      if (remainingAmount <= 0) break;

      double pDue = due.getTotalPrincipalDue();
      double iDue = due.getTotalInterestDue();
      double prePaymentTotalDue = due.getTotalDue();

      double pSettled = 0, iSettled = 0, cSettled = 0;

      // 1. Settle Principal
      if (remainingAmount >= pDue) {
        pSettled = pDue;
        remainingAmount = round(remainingAmount - pDue);
        due.setTotalPrincipalDue(0.0);
      } else {
        pSettled = remainingAmount;
        due.setTotalPrincipalDue(round(pDue - remainingAmount));
        remainingAmount = 0;
      }

      // 2. Settle Interest
      if (remainingAmount > 0) {
        if (remainingAmount >= iDue) {
          iSettled = iDue;
          remainingAmount = round(remainingAmount - iDue);
          due.setTotalInterestDue(0.0);
        } else {
          iSettled = remainingAmount;
          due.setTotalInterestDue(round(iDue - remainingAmount));
          remainingAmount = 0;
        }
      }

      // 3. Settle Global Penal Charges
      if (remainingAmount > 0 && !globalChargesPaid && lanCharge != null && lanCharge.getPenalCharges() > 0) {
        double cDue = lanCharge.getPenalCharges();
        if (remainingAmount >= cDue) {
          cSettled += cDue;
          remainingAmount = round(remainingAmount - cDue);
          lanCharge.setPenalCharges(0.0);
        } else {
          cSettled += remainingAmount;
          lanCharge.setPenalCharges(round(cDue - remainingAmount));
          remainingAmount = 0;
        }
        lanChargeRepository.save(lanCharge);
      }

      // 4. Settle Global Other Fees (Cancellation/Foreclosure)
      if (remainingAmount > 0 && !globalChargesPaid && lanCharge != null && lanCharge.getOtherFees() > 0) {
        double fDue = lanCharge.getOtherFees();
        if (remainingAmount >= fDue) {
          cSettled += fDue;
          remainingAmount = round(remainingAmount - fDue);
          lanCharge.setOtherFees(0.0);
        } else {
          cSettled += remainingAmount;
          lanCharge.setOtherFees(round(fDue - remainingAmount));
          remainingAmount = 0;
        }
        lanChargeRepository.save(lanCharge);
      }

      if (!globalChargesPaid) {
          globalChargesPaid = true;
      }

      due.setTotalDue(round(due.getTotalPrincipalDue() + due.getTotalInterestDue()));

      if (due.getTotalDue() <= 0) {
        due.setStatus(RepaymentStatus.PAID);
      }
      repaymentSchedulerRepository.save(due);

      totalPrinDerived = round(totalPrinDerived + pSettled);
      totalIntDerived = round(totalIntDerived + iSettled);
      totalCharDerived = round(totalCharDerived + cSettled);

      SettlementAudit audit =
          SettlementAudit.builder()
              .repaymentScheduler(due)
              .loanCredit(credit)
              .loanAccount(account)
              .dueDate(due.getDueDate())
              .dueForThisMonth(round(globalDueForThisMonth))
              .chargesDue(round(globalChargesDue))
              .dueFromPreviousMonths(round(globalDueFromPreviousMonths))
              .totalDue(round(globalTotalDue))
              .amountDerived(round(pSettled + iSettled + cSettled))
              .principleDerived(round(pSettled))
              .interestDerived(round(iSettled))
              .chargesDerived(round(cSettled))
              .dateOfCredit(dateOfCredit)
              .isSettled(due.getStatus() == RepaymentStatus.PAID)
              .status("SUCCESS")
              .build();
      settlementAuditRepository.save(audit);

      if (due.getDueDate().isBefore(dateOfCredit)) {
        globalDueFromPreviousMonths -= (pSettled + iSettled);
      } else {
        globalDueForThisMonth -= (pSettled + iSettled);
      }
      globalChargesDue -= cSettled;
      globalTotalDue = globalDueForThisMonth + globalDueFromPreviousMonths + globalChargesDue;
    }

    // If there were no dues, but there were global charges, we should settle them here!
    if (remainingAmount > 0 && !globalChargesPaid && lanCharge != null) {
        double cSettled = 0;

        if (lanCharge.getPenalCharges() > 0) {
            double cDue = lanCharge.getPenalCharges();
            if (remainingAmount >= cDue) {
              cSettled += cDue;
              remainingAmount = round(remainingAmount - cDue);
              lanCharge.setPenalCharges(0.0);
            } else {
              cSettled += remainingAmount;
              lanCharge.setPenalCharges(round(cDue - remainingAmount));
              remainingAmount = 0;
            }
        }

        if (remainingAmount > 0 && lanCharge.getOtherFees() > 0) {
            double fDue = lanCharge.getOtherFees();
            if (remainingAmount >= fDue) {
              cSettled += fDue;
              remainingAmount = round(remainingAmount - fDue);
              lanCharge.setOtherFees(0.0);
            } else {
              cSettled += remainingAmount;
              lanCharge.setOtherFees(round(fDue - remainingAmount));
              remainingAmount = 0;
            }
        }
        lanChargeRepository.save(lanCharge);
        totalCharDerived = round(totalCharDerived + cSettled);
    }

    credit.setTotalPrincipleDerived(totalPrinDerived);
    credit.setTotalInterestDerived(totalIntDerived);
    credit.setTotalChargesDerived(totalCharDerived);
    credit.setStatus("SUCCESS");

    loanCreditRepository.save(credit);


    updateLoanAccountDue(account.getLan());
    
    if (cacheManager != null) {
        Long lan = account.getLan();
        if (cacheManager.getCache("loanCredits") != null) cacheManager.getCache("loanCredits").evict(lan);
        if (cacheManager.getCache("allLoanCredits") != null) cacheManager.getCache("allLoanCredits").clear();
        
        if (cacheManager.getCache("repaymentSchedules") != null) cacheManager.getCache("repaymentSchedules").evict(lan);
        if (cacheManager.getCache("allRepaymentSchedules") != null) cacheManager.getCache("allRepaymentSchedules").clear();
        
        if (cacheManager.getCache("settlementAudits") != null) cacheManager.getCache("settlementAudits").evict(lan);
        if (cacheManager.getCache("allSettlementAudits") != null) cacheManager.getCache("allSettlementAudits").clear();
        
        if (cacheManager.getCache("loanDues") != null) cacheManager.getCache("loanDues").evict(lan);
        if (cacheManager.getCache("lanCharges") != null) cacheManager.getCache("lanCharges").evict(lan);
        
        if (cacheManager.getCache("loanAccount") != null) cacheManager.getCache("loanAccount").evict(lan);
        if (cacheManager.getCache("loanAccounts") != null) cacheManager.getCache("loanAccounts").clear();
    }
    
    return credit;
  }

  private void updateLoanAccountDue(Long lan) {
    log.info("Recalculating LoanAccountDue ledger for LAN: {}", lan);
    // Recalculate totals from RepaymentScheduler and update LoanAccountDue
    List<RepaymentScheduler> remainingDues =
        repaymentSchedulerRepository.findByLoanAccount_LanAndStatusOrderByDueDateAsc(
            lan, RepaymentStatus.PENDING);

    LoanAccountDue due = loanAccountDueRepository.findById(lan).orElse(null);
    if (due != null) {
      LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
      double globalCharges = lanCharge != null ? (lanCharge.getPenalCharges() + lanCharge.getOtherFees()) : 0.0;

      if (remainingDues.isEmpty() && globalCharges <= 0) {
        due.setIsSettled(true);
        due.setTotalOutstandingAmount(0.0);
        due.setTotalOutstandingPrinciple(0.0);
        due.setTotalOutstandingInterest(0.0);
        due.setTotalChargesDue(0.0);

        LoanAccount account = loanAccountRepository.findById(lan).orElse(null);
        if (account != null) {
          if (account.getStatus() != LoanStatus.PENDING_CANCELLATION && account.getStatus() != LoanStatus.PENDING_FORECLOSURE) {
            account.setStatus(LoanStatus.CLOSED);
          }
          loanAccountRepository.save(account);
        }
      } else {
        double totalOutAmt = 0, totalOutP = 0, totalOutI = 0;
        for (RepaymentScheduler r : remainingDues) {
          totalOutAmt += r.getTotalDue();
          totalOutP += r.getTotalPrincipalDue();
          totalOutI += r.getTotalInterestDue();
        }

        if (!remainingDues.isEmpty()) {
          RepaymentScheduler nextDue = remainingDues.get(0);
          due.setNextDueDate(nextDue.getDueDate());
          due.setNextDueAmount(round(nextDue.getTotalDue() + globalCharges));
          due.setNextDuePrinciple(round(nextDue.getTotalPrincipalDue()));
          due.setNetDueInterest(round(nextDue.getTotalInterestDue()));
        } else {
          due.setNextDueDate(LocalDate.now());
          due.setNextDueAmount(round(globalCharges));
          due.setNextDuePrinciple(0.0);
          due.setNetDueInterest(0.0);
        }
        due.setNextDueCharges(round(globalCharges));

        due.setTotalOutstandingAmount(round(totalOutAmt + globalCharges));
        due.setTotalOutstandingPrinciple(round(totalOutP));
        due.setTotalOutstandingInterest(round(totalOutI));
        due.setTotalChargesDue(round(globalCharges));
        due.setIsSettled(false);
      }
      loanAccountDueRepository.save(due);
    }
  }

  @Cacheable(value = "allRepaymentSchedules")
  public List<RepaymentScheduler> getAllSchedules() {
    log.info("Service fetching all repayment schedules");
    return repaymentSchedulerRepository.findAll();
  }

  @Cacheable(value = "repaymentSchedules", key = "#lan")
  public List<RepaymentScheduler> getSchedulesByLan(Long lan) {
    log.info("Service fetching repayment schedules for LAN: {}", lan);
    return repaymentSchedulerRepository.findByLoanAccount_LanOrderByDueDateAsc(lan);
  }

  @Cacheable(value = "allLoanCredits")
  public List<LoanCredit> getAllCredits() {
    log.info("Service fetching all loan credits");
    return loanCreditRepository.findAll();
  }

  @Cacheable(value = "loanCredits", key = "#lan")
  public List<LoanCredit> getCreditsByLan(Long lan) {
    log.info("Service fetching loan credits for LAN: {}", lan);
    return loanCreditRepository.findByLoanAccount_Lan(lan);
  }

  @Cacheable(value = "allSettlementAudits")
  public List<SettlementAudit> getAllAudits() {
    log.info("Service fetching all settlement audits");
    return settlementAuditRepository.findAll();
  }

  @Cacheable(value = "settlementAudits", key = "#lan")
  public List<SettlementAudit> getAuditsByLan(Long lan) {
    log.info("Service fetching settlement audits for LAN: {}", lan);
    return settlementAuditRepository.findByLoanAccount_Lan(lan);
  }

  private double round(double value) {
    return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
  }
}
