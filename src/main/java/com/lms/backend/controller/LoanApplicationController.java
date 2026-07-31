package com.lms.backend.controller;

import com.lms.backend.dto.ApplyRequest;
import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.InquireRequest;
import com.lms.backend.dto.LanChargeResponse;
import com.lms.backend.dto.LoanAccountDueResponse;
import com.lms.backend.dto.LoanAccountResponse;
import com.lms.backend.dto.LoanResponse;
import com.lms.backend.enums.LoanStatus;
import com.lms.backend.service.LoanApplicationService;
import com.lms.backend.service.CancellationService;
import com.lms.backend.service.ForeclosureService;
import com.lms.backend.service.ReactivationService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/lms")
public class LoanApplicationController {

  @Autowired private LoanApplicationService loanApplicationService;
  @Autowired private CancellationService cancellationService;
  @Autowired private ForeclosureService foreclosureService;
  @Autowired private ReactivationService reactivationService;


  @PostMapping("/inquire")
  public ResponseEntity<List<LoanResponse>> inquire(@Valid @RequestBody InquireRequest request) {
    log.info("Received loan inquiry request: {}", request);
    return ResponseEntity.ok(loanApplicationService.inquire(request).stream().map(LoanResponse::fromEntity).collect(Collectors.toList()));
  }

  @PostMapping("/apply")
  public ResponseEntity<LoanAccountResponse> apply(@Valid @RequestBody ApplyRequest request) {
    log.info("Received loan application request for user: {}", request.getUserId());
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(loanApplicationService.apply(request)));
  }

  @PutMapping("/{lan}/verify")
  public ResponseEntity<LoanAccountResponse> verifyStatus(
      @PathVariable Long lan, @RequestParam LoanStatus status) {
    log.info("Verifying status for LAN {}: {}", lan, status);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(loanApplicationService.verifyStatus(lan, status)));
  }

  @PostMapping("/{lan}/cancel")
  public ResponseEntity<LoanAccountResponse> cancelLoan(
      @PathVariable Long lan, 
      @RequestParam(required = false) java.time.LocalDate dateOfCancellation) {
    log.info("Received request to cancel loan LAN {}", lan);
    if (dateOfCancellation == null) {
      dateOfCancellation = java.time.LocalDate.now();
    }
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(cancellationService.cancelLoan(lan, dateOfCancellation)));
  }

  @PutMapping("/{lan}/verify-cancellation")
  public ResponseEntity<LoanAccountResponse> verifyCancellation(@PathVariable Long lan) {
    log.info("Received request to verify cancellation for LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(cancellationService.verifyCancellation(lan)));
  }

  @PutMapping("/{lan}/verify-foreclosure")
  public ResponseEntity<LoanAccountResponse> verifyForeclosure(@PathVariable Long lan) {
    log.info("Received request to verify foreclosure for LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(foreclosureService.verifyForeclosure(lan)));
  }

  @PostMapping("/{lan}/foreclose")
  public ResponseEntity<LoanAccountResponse> forecloseLoan(@PathVariable Long lan) {
    log.info("Received request to foreclose loan for LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(foreclosureService.forecloseLoan(lan)));
  }

  @GetMapping("/accounts")
  public ResponseEntity<List<LoanAccountResponse>> getAllLoanAccounts() {
    log.info("Fetching all loan accounts");
    return ResponseEntity.ok(loanApplicationService.getAllLoanAccounts().stream().map(LoanAccountResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/accounts/{lan}/details")
  public ResponseEntity<LoanAccountResponse> getLoanAccount(@PathVariable Long lan) {
    log.info("Fetching details for loan account LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(loanApplicationService.getLoanAccount(lan)));
  }

  @GetMapping("/accounts/{lan}/dues")
  public ResponseEntity<LoanAccountDueResponse> getLoanAccountDue(@PathVariable Long lan) {
    log.info("Fetching due ledger for loan account LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountDueResponse.fromEntity(loanApplicationService.getLoanAccountDue(lan)));
  }

  @GetMapping("/accounts/{lan}/dues-summary")
  public ResponseEntity<com.lms.backend.dto.LoanAccountDueSummaryResponse> getLoanAccountDueSummary(@PathVariable Long lan) {
    log.info("Fetching due summary for loan account LAN: {}", lan);
    java.util.Map<String, Object> status = loanApplicationService.getNextDueStatus(lan, null);
    
    return ResponseEntity.ok(new com.lms.backend.dto.LoanAccountDueSummaryResponse(
        ((Number) status.get("principal")).doubleValue(),
        ((Number) status.get("interest")).doubleValue(),
        ((Number) status.get("charges")).doubleValue(),
        ((Number) status.get("totalDue")).doubleValue()
    ));
  }

  @PostMapping("/accounts/{lan}/calculate-dpd")
  public ResponseEntity<LoanAccountResponse> calculateDpdAndPenalties(@PathVariable Long lan) {
    log.info("Triggering explicit DPD and penalty calculation for LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(loanApplicationService.calculateDpdAndPenalties(lan, java.time.LocalDate.now())));
  }

  @GetMapping("/accounts/{lan}/next-due-status")
  public ResponseEntity<java.util.Map<String, Object>> getNextDueStatus(
      @PathVariable Long lan,
      @RequestParam(required = false) java.time.LocalDate date) {
    log.info("Fetching next due status for LAN: {} relative to date: {}", lan, date);
    return ResponseEntity.ok(loanApplicationService.getNextDueStatus(lan, date));
  }

  @PutMapping("/accounts/{lan}/activate")
  public ResponseEntity<LoanAccountResponse> activateAccount(@PathVariable Long lan) {
    log.info("Received request to activate pending cancelled/foreclosed account for LAN: {}", lan);
    return ResponseEntity.ok(LoanAccountResponse.fromEntity(reactivationService.activateAccount(lan)));
  }

  @GetMapping("/accounts/{lan}/charges")
  public ResponseEntity<LanChargeResponse> getLanCharge(@PathVariable Long lan) {
    log.info("Fetching LanCharge details for LAN: {}", lan);
    return ResponseEntity.ok(LanChargeResponse.fromEntity(loanApplicationService.getLanCharge(lan)));
  }


}
