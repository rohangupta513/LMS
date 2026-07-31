package com.lms.backend.service;

import com.lms.backend.dto.ApplyRequest;
import com.lms.backend.dto.InquireRequest;
import com.lms.backend.entity.*;
import com.lms.backend.exception.ResourceNotFoundException;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.enums.LoanType;
import com.lms.backend.enums.RepaymentStatus;
import com.lms.backend.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoanApplicationService {

  @Autowired private LoanRepository loanRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private LenderRepository lenderRepository;
  @Autowired private LoanAccountRepository loanAccountRepository;
  @Autowired private LoanAccountDueRepository loanAccountDueRepository;
  @Autowired private RepaymentSchedulerRepository repaymentSchedulerRepository;
  @Autowired private LoanCreditRepository loanCreditRepository;
  @Autowired private LanChargeRepository lanChargeRepository;
  @Autowired private SettlementAuditRepository settlementAuditRepository;
  @Autowired private CacheManager cacheManager;

  public List<Loan> inquire(InquireRequest request) {
    log.info("Inquiring loans for amount: {} and type: {}", request.getAmount(), request.getTypeOfLoan());
    return loanRepository.findByCriteria(
        request.getAmount(),
        request.getRateOfInterest(),
        request.getTimePeriod(),
        request.getTypeOfLoan());
  }


  @CacheEvict(value = "loanAccounts", allEntries = true)
  public LoanAccount apply(ApplyRequest request) {
    log.info("Processing loan application for user ID: {}", request.getUserId());
    User user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Lender lender =
        lenderRepository
            .findById(request.getLenderId())
            .orElseThrow(() -> new ResourceNotFoundException("Lender not found"));
    Loan loan =
        loanRepository
            .findById(request.getLoanId())
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

    if (request.getAmount() < loan.getLoanAmountMin() || request.getAmount() > loan.getLoanAmountMax()) {
      throw new RuntimeException(String.format("Loan amount (%.2f) must be between %.2f and %.2f.",
          request.getAmount(), loan.getLoanAmountMin(), loan.getLoanAmountMax()));
    }
    if (request.getRateOfInterest() < loan.getLoanInterestMin() || request.getRateOfInterest() > loan.getLoanInterestMax()) {
      throw new RuntimeException(String.format("Rate of interest (%.2f%%) must be between %.2f%% and %.2f%%.",
          request.getRateOfInterest(), loan.getLoanInterestMin(), loan.getLoanInterestMax()));
    }
    if (request.getTimePeriod() < loan.getLoanTimeMin() || request.getTimePeriod() > loan.getLoanTimeMax()) {
      throw new RuntimeException(String.format("Time period (%d months) must be between %d and %d months.",
          request.getTimePeriod(), loan.getLoanTimeMin(), loan.getLoanTimeMax()));
    }

    long activeLoansCount = loanAccountRepository.countActiveLoansByUserId(
        user.getUserId(),
        java.util.Arrays.asList(LoanStatus.CANCELLED, LoanStatus.FORECLOSED)
    );

    if (activeLoansCount >= 3) {
      throw new RuntimeException("Maximum loan limit reached. A user can have a maximum of 3 loans at a time.");
    }

    LoanAccount account =
        LoanAccount.builder()
            .user(user)
            .lender(lender)
            .loan(loan)
            .amount(request.getAmount())
            .rateOfInterest(request.getRateOfInterest())
            .timePeriod(request.getTimePeriod())
            .startDate(request.getApplicationDate() != null ? request.getApplicationDate() : LocalDate.now())
            .status(LoanStatus.LOAN_APPLIED)
            .build();

    return loanAccountRepository.save(account);
  }


  @Caching(evict = { 
    @CacheEvict(value = "loanAccount", key = "#lan"), 
    @CacheEvict(value = "loanAccounts", allEntries = true) 
  })
  @Transactional
  public LoanAccount verifyStatus(Long lan, LoanStatus status) {
    LoanAccount account =
        loanAccountRepository
            .findById(lan)
            .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));
    account.setStatus(status);
    loanAccountRepository.save(account);

    if (status == LoanStatus.ACTIVE) {
      generateRepaymentSchedule(account);
    }

    return account;
  }

  private void generateRepaymentSchedule(LoanAccount account) {
    double principal = account.getAmount();
    double rate = account.getRateOfInterest();
    int time = account.getTimePeriod();
    LoanType type = account.getLoan().getTypeOfLoan();

    double simpleInterest = (principal * rate * time) / 100;
    double totalDue = principal + simpleInterest;

    int numDues = time; // assuming time = months for personal, days for merchant

    double principalPerDue = Math.round((principal / numDues) * 100.0) / 100.0;
    double interestPerDue = Math.round((simpleInterest / numDues) * 100.0) / 100.0;
    double amountPerDue = Math.round((totalDue / numDues) * 100.0) / 100.0;

    LocalDate startDate = account.getStartDate();

    for (int i = 1; i <= numDues; i++) {
      LocalDate dueDate =
          (type == LoanType.PERSONAL) ? startDate.plusMonths(i) : startDate.plusDays(i);

      RepaymentScheduler rs =
          RepaymentScheduler.builder()
              .loanAccount(account)
              .dueDate(dueDate)
              .totalPrincipalDue(principalPerDue)
              .totalInterestDue(interestPerDue)
              .totalDue(amountPerDue)
              .status(RepaymentStatus.PENDING)
              .build();
      repaymentSchedulerRepository.save(rs);
    }

    // Initialize LoanAccountDue
    LoanAccountDue due =
        LoanAccountDue.builder()
            .loanAccount(account)
            .nextDueDate(
                (type == LoanType.PERSONAL) ? startDate.plusMonths(1) : startDate.plusDays(1))
            .nextDueAmount(amountPerDue)
            .nextDuePrinciple(principalPerDue)
            .netDueInterest(interestPerDue)
            .nextDueCharges(0.0)
            .totalOutstandingAmount(totalDue)
            .totalOutstandingPrinciple(principal)
            .totalOutstandingInterest(simpleInterest)
            .totalDerivedAmount(0.0)
            .totalDerivedPrinciple(0.0)
            .totalDerivedInterest(0.0)
            .totalChargesDue(0.0)
            .totalChargesDerived(0.0)
            .isSettled(false)
            .isCancelled(false)
            .isForeclosed(false)
            .build();
    loanAccountDueRepository.save(due);
  }


  @Cacheable(value = "loanAccount", key = "#lan")
  public LoanAccount getLoanAccount(Long lan) {
    return loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));
  }

  @Cacheable(value = "loanDues", key = "#lan")
  public LoanAccountDue getLoanAccountDue(Long lan) {
    return loanAccountDueRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account Due not found"));
  }

  @Cacheable(value = "loanAccounts")
  public List<LoanAccount> getAllLoanAccounts() {
    return loanAccountRepository.findAll();
  }

  public LoanAccount calculateDpdAndPenalties(Long lan, LocalDate relativeDate) {
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.ACTIVE) {
      return account; // Only calculate penalties for active ACTIVE loans
    }

    List<RepaymentScheduler> dues = repaymentSchedulerRepository
        .findByLoanAccount_LanAndStatusOrderByDueDateAsc(lan, RepaymentStatus.PENDING);

    LocalDate calculationDate = relativeDate != null ? relativeDate : LocalDate.now();
    
    // Find oldest pending EMI to determine Account DPD
    int accountDpd = 0;
    if (!dues.isEmpty()) {
      RepaymentScheduler oldest = dues.get(0);
      if (oldest.getDueDate().isBefore(calculationDate)) {
        accountDpd = (int) java.time.temporal.ChronoUnit.DAYS.between(oldest.getDueDate(), calculationDate);
      }
    }

    // Fetch or create LanCharge
    LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
    if (lanCharge == null) {
      lanCharge = new LanCharge();
      lanCharge.setLan(lan);
      lanCharge.setDpd(0);
      if (accountDpd > 0) {
        lanCharge.setPenalCharges(accountDpd * 10.0);
        lanCharge.setOtherFees(0.0);
        lanCharge.setLastCalculatedDate(calculationDate);
      } else {
        lanCharge.setPenalCharges(0.0);
        lanCharge.setOtherFees(0.0);
        lanCharge.setLastCalculatedDate(account.getStartDate() != null ? account.getStartDate() : LocalDate.now());
      }
    }

    // If penal charges were wiped (e.g. during cancellation) and DPD > 0, 
    // recalculate them fully from the oldest due date.
    if (accountDpd > 0 && lanCharge.getPenalCharges() == 0.0) {
        lanCharge.setPenalCharges(accountDpd * 10.0);
        lanCharge.setLastCalculatedDate(calculationDate);
    }

    lanCharge.setDpd(accountDpd);

    if (accountDpd > 0 && lanCharge.getLastCalculatedDate() != null && lanCharge.getLastCalculatedDate().isBefore(calculationDate)) {
      long daysToCharge = java.time.temporal.ChronoUnit.DAYS.between(lanCharge.getLastCalculatedDate(), calculationDate);
      if (daysToCharge > 0) {
        lanCharge.setPenalCharges(round(lanCharge.getPenalCharges() + (daysToCharge * 10.0)));
        lanCharge.setLastCalculatedDate(calculationDate);
      }
    } else if (lanCharge.getLastCalculatedDate() == null) {
      lanCharge.setLastCalculatedDate(calculationDate);
    }
    
    lanChargeRepository.save(lanCharge);

    // Recalculate ledger totals
    LoanAccountDue dueLedger = loanAccountDueRepository.findById(lan).orElse(null);
    if (dueLedger != null && !dues.isEmpty()) {
        RepaymentScheduler nextDue = dues.get(0);
        dueLedger.setNextDueDate(nextDue.getDueDate());
        dueLedger.setNextDueAmount(nextDue.getTotalDue());
        dueLedger.setNextDuePrinciple(nextDue.getTotalPrincipalDue());
        dueLedger.setNetDueInterest(nextDue.getTotalInterestDue());
        dueLedger.setNextDueCharges(lanCharge.getPenalCharges() + lanCharge.getOtherFees());

        double totalOutAmt = 0, totalOutP = 0, totalOutI = 0;
        for (RepaymentScheduler r : dues) {
          totalOutAmt += r.getTotalDue();
          totalOutP += r.getTotalPrincipalDue();
          totalOutI += r.getTotalInterestDue();
        }
        dueLedger.setTotalOutstandingAmount(round(totalOutAmt + lanCharge.getPenalCharges() + lanCharge.getOtherFees()));
        dueLedger.setTotalOutstandingPrinciple(round(totalOutP));
        dueLedger.setTotalOutstandingInterest(round(totalOutI));
        dueLedger.setTotalChargesDue(round(lanCharge.getPenalCharges() + lanCharge.getOtherFees()));
        loanAccountDueRepository.save(dueLedger);
    }
    
    if (cacheManager != null) {
      if (cacheManager.getCache("lanCharges") != null) cacheManager.getCache("lanCharges").evict(lan);
      if (cacheManager.getCache("loanDues") != null) cacheManager.getCache("loanDues").evict(lan);
    }
    
    return account;
  }

  private double round(double value) {
    return java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
  }

  public java.util.Map<String, Object> getNextDueStatus(Long lan, LocalDate dateOfCredit) {
    LocalDate calcDate = dateOfCredit != null ? dateOfCredit : LocalDate.now();
    LoanAccount account = loanAccountRepository.findById(lan)
        .orElseThrow(() -> new ResourceNotFoundException("Loan Account not found"));

    if (account.getStatus() != LoanStatus.PENDING_CANCELLATION && account.getStatus() != LoanStatus.PENDING_FORECLOSURE) {
      calculateDpdAndPenalties(lan, calcDate);
    }
    
    List<RepaymentScheduler> dues = repaymentSchedulerRepository
        .findByLoanAccount_LanAndStatusOrderByDueDateAsc(lan, RepaymentStatus.PENDING);

    java.util.Map<String, Object> response = new java.util.HashMap<>();
    
    LanCharge lanCharge = lanChargeRepository.findById(lan).orElse(null);
    double globalCharges = lanCharge != null ? (lanCharge.getPenalCharges() + lanCharge.getOtherFees()) : 0.0;
    int globalDpd = lanCharge != null ? lanCharge.getDpd() : 0;

    if (dues.isEmpty()) {
      response.put("dpd", globalDpd);
      response.put("totalDue", globalCharges);
      response.put("principal", 0.0);
      response.put("interest", 0.0);
      response.put("charges", globalCharges);
      response.put("dueDate", "-");
      return response;
    }

    double totalDue = 0, principal = 0, interest = 0;
    String lastDueDate = "";
    boolean foundAny = false;

    for (RepaymentScheduler due : dues) {
      if (account.getStatus() == LoanStatus.PENDING_FORECLOSURE || 
          account.getStatus() == LoanStatus.PENDING_CANCELLATION || 
          !due.getDueDate().isAfter(calcDate)) {
         foundAny = true;
         totalDue += due.getTotalDue();
         principal += due.getTotalPrincipalDue();
         interest += due.getTotalInterestDue();
         lastDueDate = due.getDueDate().toString();
      }
    }

    if (!foundAny) {
      // No dues are strictly due as of the calcDate (e.g. they paid early)
      RepaymentScheduler nextDue = dues.get(0);
      response.put("dpd", globalDpd);
      response.put("totalDue", globalCharges);
      response.put("principal", 0.0);
      response.put("interest", 0.0);
      response.put("charges", globalCharges);
      response.put("dueDate", nextDue.getDueDate().toString());
    } else {
      response.put("dpd", globalDpd);
      response.put("totalDue", totalDue + globalCharges);
      response.put("principal", principal);
      response.put("interest", interest);
      response.put("charges", globalCharges);
      response.put("dueDate", lastDueDate);
    }
    return response;
  }
  public String settleLoanManually(Long lan) {
    LoanAccountDue due = loanAccountDueRepository.findById(lan).orElseThrow();
    double totalOutstanding = due.getTotalOutstandingAmount();
    if (totalOutstanding <= 0) {
      due.setIsSettled(true);
      loanAccountDueRepository.save(due);
      
      LoanAccount acc = loanAccountRepository.findById(lan).orElseThrow();
      if (acc.getStatus() == LoanStatus.PENDING_FORECLOSURE) {
        acc.setStatus(LoanStatus.FORECLOSED);
        loanAccountRepository.save(acc);
      }
    }
    return totalOutstanding > 0 ? "You have " + totalOutstanding + " remaining to settle the loan." : "Loan is fully settled.";
  }

  @Cacheable(value = "lanCharges", key = "#lan")
  public LanCharge getLanCharge(Long lan) {
    return lanChargeRepository.findById(lan).orElse(null);
  }
}
