package com.lms.backend.controller;

import com.lms.backend.dto.CreditRequest;
import com.lms.backend.entity.LoanCredit;
import com.lms.backend.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/settlement")
/**
 * REST Controller handling incoming HTTP requests for SettlementController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class SettlementController {

  @Autowired private SettlementService settlementService;

  @PostMapping("/credit")
  public ResponseEntity<?> processCredit(@RequestBody CreditRequest request) {
    log.info("Received credit payment request for LAN: {}", request.getLan());
    try {
      return ResponseEntity.ok(settlementService.processCredit(request));
    } catch (RuntimeException e) {
      log.error("Failed to process credit for LAN {}: {}", request.getLan(), e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/credit/{credId}/verify")
  public ResponseEntity<LoanCredit> verifyCredit(@PathVariable Long credId) {
    log.info("Verifying credit ID: {}", credId);
    return ResponseEntity.ok(settlementService.verifyCredit(credId));
  }

  @GetMapping("/schedules")
  public ResponseEntity<java.util.List<com.lms.backend.entity.RepaymentScheduler>> getAllSchedules() {
    log.info("Fetching all repayment schedules");
    return ResponseEntity.ok(settlementService.getAllSchedules());
  }

  @GetMapping("/schedules/{lan}")
  public ResponseEntity<java.util.List<com.lms.backend.entity.RepaymentScheduler>> getSchedulesByLan(@PathVariable Long lan) {
    log.info("Fetching repayment schedules for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getSchedulesByLan(lan));
  }

  @GetMapping("/credits")
  public ResponseEntity<java.util.List<LoanCredit>> getAllCredits() {
    log.info("Fetching all loan credits");
    return ResponseEntity.ok(settlementService.getAllCredits());
  }

  @GetMapping("/credits/{lan}")
  public ResponseEntity<java.util.List<LoanCredit>> getCreditsByLan(@PathVariable Long lan) {
    log.info("Fetching loan credits for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getCreditsByLan(lan));
  }

  @GetMapping("/audits")
  public ResponseEntity<java.util.List<com.lms.backend.entity.SettlementAudit>> getAllAudits() {
    log.info("Fetching all settlement audits");
    return ResponseEntity.ok(settlementService.getAllAudits());
  }

  @GetMapping("/audits/{lan}")
  public ResponseEntity<java.util.List<com.lms.backend.entity.SettlementAudit>> getAuditsByLan(@PathVariable Long lan) {
    log.info("Fetching settlement audits for LAN: {}", lan);
    return ResponseEntity.ok(settlementService.getAuditsByLan(lan));
  }
}
