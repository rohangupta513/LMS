package com.lms.backend.controller;

import com.lms.backend.entity.Loan;
import com.lms.backend.service.LoanService;
import java.util.List;
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

  @PostMapping
  public ResponseEntity<Loan> add(@RequestBody Loan u) {
    return ResponseEntity.ok(service.add(u));
  }

  @GetMapping
  public ResponseEntity<List<Loan>> getAll() {
    return ResponseEntity.ok(service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Loan> get(@PathVariable Long id) {
    return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Loan> update(@PathVariable Long id, @RequestBody Loan u) {
    try {
      return ResponseEntity.ok(service.update(id, u));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
