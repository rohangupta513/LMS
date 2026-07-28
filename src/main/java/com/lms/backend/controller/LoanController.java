package com.lms.backend.controller;

import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.LoanResponse;
import com.lms.backend.entity.Loan;
import com.lms.backend.service.LoanService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
/**
 * REST Controller handling incoming HTTP requests for LoanController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class LoanController {
  @Autowired private LoanService service;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(400, e.getMessage()));
  }

  @PostMapping
  public ResponseEntity<LoanResponse> add(@RequestBody Loan l) {
    return ResponseEntity.ok(LoanResponse.fromEntity(service.add(l)));
  }

  @GetMapping
  public ResponseEntity<List<LoanResponse>> getAll() {
    return ResponseEntity.ok(service.getAll().stream().map(LoanResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<LoanResponse> get(@PathVariable Long id) {
    return service.get(id).map(l -> ResponseEntity.ok(LoanResponse.fromEntity(l))).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<LoanResponse> update(@PathVariable Long id, @RequestBody Loan l) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setLenderId(l.getLenderId());
              existing.setLoanAmountMin(l.getLoanAmountMin());
              existing.setLoanAmountMax(l.getLoanAmountMax());
              existing.setLoanInterestMin(l.getLoanInterestMin());
              existing.setLoanInterestMax(l.getLoanInterestMax());
              existing.setLoanTimeMin(l.getLoanTimeMin());
              existing.setLoanTimeMax(l.getLoanTimeMax());
              existing.setTypeOfLoan(l.getTypeOfLoan());
              return ResponseEntity.ok(LoanResponse.fromEntity(service.add(existing)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
