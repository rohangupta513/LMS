package com.lms.backend.controller;

import com.lms.backend.dto.CreditRequest;
import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.LoanCreditResponse;
import com.lms.backend.dto.RepaymentSchedulerResponse;
import com.lms.backend.dto.SettlementAuditResponse;
import com.lms.backend.service.SettlementService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/settlement")

public class SettlementController {

  @Autowired private SettlementService settlementService;


  @PostMapping("/credit")
  public ResponseEntity<LoanCreditResponse> processCredit(@Valid @RequestBody CreditRequest request) {
    log.info("Received credit payment request for LAN: {}", request.getLan());
    return ResponseEntity.ok(LoanCreditResponse.fromEntity(settlementService.processCredit(request)));
  }

  @PutMapping("/credit/{credId}/verify")
  public ResponseEntity<LoanCreditResponse> verifyCredit(@PathVariable Long credId) {
    log.info("Verifying credit ID: {}", credId);
    return ResponseEntity.ok(LoanCreditResponse.fromEntity(settlementService.verifyCredit(credId)));
  }

  @GetMapping("/schedules")
  public ResponseEntity<List<RepaymentSchedulerResponse>> getAllSchedules() {
    log.info("Fetching all repayment schedules");
    return ResponseEntity.ok(settlementService.getAllSchedules().stream().map(RepaymentSchedulerResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/schedules/{lan}")
  public ResponseEntity<List<RepaymentSchedulerResponse>> getSchedulesByLan(@PathVariable Long lan) {
    log.info("Fetching repayment schedules for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getSchedulesByLan(lan).stream().map(RepaymentSchedulerResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/credits")
  public ResponseEntity<List<LoanCreditResponse>> getAllCredits() {
    log.info("Fetching all loan credits");
    return ResponseEntity.ok(settlementService.getAllCredits().stream().map(LoanCreditResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/credits/{lan}")
  public ResponseEntity<List<LoanCreditResponse>> getCreditsByLan(@PathVariable Long lan) {
    log.info("Fetching loan credits for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getCreditsByLan(lan).stream().map(LoanCreditResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/audits")
  public ResponseEntity<List<SettlementAuditResponse>> getAllAudits() {
    log.info("Fetching all settlement audits");
    return ResponseEntity.ok(settlementService.getAllAudits().stream().map(SettlementAuditResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/audits/{lan}")
  public ResponseEntity<List<SettlementAuditResponse>> getAuditsByLan(@PathVariable Long lan) {
    log.info("Fetching settlement audits for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getAuditsByLan(lan).stream().map(SettlementAuditResponse::fromEntity).collect(Collectors.toList()));
  }
}
