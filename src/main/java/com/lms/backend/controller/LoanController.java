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
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loans")

public class LoanController {
  @Autowired private LoanService service;


  @PostMapping
  public ResponseEntity<LoanResponse> add(@Valid @RequestBody Loan l) {
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
  public ResponseEntity<LoanResponse> update(@PathVariable Long id, @Valid @RequestBody Loan l) {
    return service
        .get(id)
        .map(
            existing -> {
              if (l.getLenderId() != null) existing.setLenderId(l.getLenderId());
              if (l.getLoanAmountMin() != null) existing.setLoanAmountMin(l.getLoanAmountMin());
              if (l.getLoanAmountMax() != null) existing.setLoanAmountMax(l.getLoanAmountMax());
              if (l.getLoanInterestMin() != null) existing.setLoanInterestMin(l.getLoanInterestMin());
              if (l.getLoanInterestMax() != null) existing.setLoanInterestMax(l.getLoanInterestMax());
              if (l.getLoanTimeMin() != null) existing.setLoanTimeMin(l.getLoanTimeMin());
              if (l.getLoanTimeMax() != null) existing.setLoanTimeMax(l.getLoanTimeMax());
              if (l.getTypeOfLoan() != null) existing.setTypeOfLoan(l.getTypeOfLoan());
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
