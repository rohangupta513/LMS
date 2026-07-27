package com.lms.backend.controller;

import com.lms.backend.entity.Lender;
import com.lms.backend.service.LenderService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lenders")
/**
 * REST Controller handling incoming HTTP requests for LenderController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class LenderController {
  @Autowired private LenderService service;

  @PostMapping
  public ResponseEntity<Lender> add(@RequestBody Lender u) {
    return ResponseEntity.ok(service.add(u));
  }

  @GetMapping
  public ResponseEntity<List<Lender>> getAll() {
    return ResponseEntity.ok(service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Lender> get(@PathVariable Long id) {
    return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Lender> update(@PathVariable Long id, @RequestBody Lender u) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setLenderName(u.getLenderName());
              existing.setLenderContact(u.getLenderContact());
              existing.setLenderDetails(u.getLenderDetails());
              return ResponseEntity.ok(service.add(existing));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
