package com.lms.backend.controller;

import com.lms.backend.dto.ApplyRequest;
import com.lms.backend.dto.InquireRequest;
import com.lms.backend.entity.Loan;
import com.lms.backend.entity.LoanAccount;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.service.LoanApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/lms")
/**
 * REST Controller handling incoming HTTP requests for LoanApplicationController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class LoanApplicationController {

  @Autowired private LoanApplicationService loanApplicationService;

  @PostMapping("/inquire")
  public ResponseEntity<List<Loan>> inquire(@RequestBody InquireRequest request) {
    log.info("Received loan inquiry request: {}", request);
    return ResponseEntity.ok(loanApplicationService.inquire(request));
  }

  @PostMapping("/apply")
  public ResponseEntity<?> apply(@RequestBody ApplyRequest request) {
    log.info("Received loan application request for user: {}", request.getUserId());
    try {
      return ResponseEntity.ok(loanApplicationService.apply(request));
    } catch (RuntimeException e) {
      log.error("Failed to apply for loan: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{lan}/verify")
  public ResponseEntity<LoanAccount> verifyStatus(
      @PathVariable Long lan, @RequestParam LoanStatus status) {
    log.info("Verifying status for LAN {}: {}", lan, status);
    return ResponseEntity.ok(loanApplicationService.verifyStatus(lan, status));
  }

  @PostMapping("/{lan}/cancel")
  public ResponseEntity<?> cancelLoan(
      @PathVariable Long lan, 
      @RequestParam(required = false) java.time.LocalDate dateOfCancellation) {
    log.info("Received request to cancel loan LAN {}", lan);
    if (dateOfCancellation == null) {
      dateOfCancellation = java.time.LocalDate.now();
    }
    try {
      return ResponseEntity.ok(loanApplicationService.cancelLoan(lan, dateOfCancellation));
    } catch (RuntimeException e) {
      log.error("Failed to cancel loan LAN {}: {}", lan, e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{lan}/verify-cancellation")
  public ResponseEntity<?> verifyCancellation(@PathVariable Long lan) {
    log.info("Received request to verify cancellation for LAN: {}", lan);
    try {
      return ResponseEntity.ok(loanApplicationService.verifyCancellation(lan));
    } catch (RuntimeException e) {
      log.error("Failed to verify cancellation for LAN {}: {}", lan, e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{lan}/verify-foreclosure")
  public ResponseEntity<?> verifyForeclosure(@PathVariable Long lan) {
    log.info("Received request to verify foreclosure for LAN: {}", lan);
    try {
      return ResponseEntity.ok(loanApplicationService.verifyForeclosure(lan));
    } catch (RuntimeException e) {
      log.error("Failed to verify foreclosure for LAN {}: {}", lan, e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/{lan}/foreclose")
  public ResponseEntity<LoanAccount> forecloseLoan(@PathVariable Long lan) {
    log.info("Received request to foreclose loan for LAN: {}", lan);
    return ResponseEntity.ok(loanApplicationService.forecloseLoan(lan));
  }

  @GetMapping("/accounts")
  public ResponseEntity<List<LoanAccount>> getAllLoanAccounts() {
    log.info("Fetching all loan accounts");
    return ResponseEntity.ok(loanApplicationService.getAllLoanAccounts());
  }

  @GetMapping("/accounts/{lan}/details")
  public ResponseEntity<LoanAccount> getLoanAccount(@PathVariable Long lan) {
    log.info("Fetching details for loan account LAN: {}", lan);
    return ResponseEntity.ok(loanApplicationService.getLoanAccount(lan));
  }

  @GetMapping("/accounts/{lan}/dues")
  public ResponseEntity<com.lms.backend.entity.LoanAccountDue> getLoanAccountDue(@PathVariable Long lan) {
    log.info("Fetching due ledger for loan account LAN: {}", lan);
    return ResponseEntity.ok(loanApplicationService.getLoanAccountDue(lan));
  }

  @PostMapping("/accounts/{lan}/calculate-dpd")
  public ResponseEntity<LoanAccount> calculateDpdAndPenalties(@PathVariable Long lan) {
    log.info("Triggering explicit DPD and penalty calculation for LAN: {}", lan);
    return ResponseEntity.ok(loanApplicationService.calculateDpdAndPenalties(lan, java.time.LocalDate.now()));
  }

  @GetMapping("/accounts/{lan}/next-due-status")
  public ResponseEntity<java.util.Map<String, Object>> getNextDueStatus(
      @PathVariable Long lan,
      @RequestParam(required = false) java.time.LocalDate date) {
    log.info("Fetching next due status for LAN: {} relative to date: {}", lan, date);
    return ResponseEntity.ok(loanApplicationService.getNextDueStatus(lan, date));
  }

  @PutMapping("/accounts/{lan}/activate")
  public ResponseEntity<LoanAccount> activateAccount(@PathVariable Long lan) {
    log.info("Received request to activate pending cancelled/foreclosed account for LAN: {}", lan);
    try {
      return ResponseEntity.ok(loanApplicationService.activateAccount(lan));
    } catch (RuntimeException e) {
      log.error("Failed to activate account for LAN {}: {}", lan, e.getMessage());
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/accounts/{lan}/charges")
  public ResponseEntity<com.lms.backend.entity.LanCharge> getLanCharge(@PathVariable Long lan) {
    log.info("Fetching LanCharge details for LAN: {}", lan);
    return ResponseEntity.ok(loanApplicationService.getLanCharge(lan));
  }
}
