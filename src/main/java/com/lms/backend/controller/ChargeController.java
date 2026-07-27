package com.lms.backend.controller;

import com.lms.backend.entity.Charge;
import com.lms.backend.service.ChargeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/charges")
/**
 * REST Controller handling incoming HTTP requests for ChargeController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class ChargeController {
  @Autowired private ChargeService service;

  @PostMapping
  public ResponseEntity<Charge> add(@RequestBody Charge u) {
    return ResponseEntity.ok(service.add(u));
  }

  @GetMapping
  public ResponseEntity<List<Charge>> getAll() {
    return ResponseEntity.ok(service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Charge> get(@PathVariable Long id) {
    return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Charge> update(@PathVariable Long id, @RequestBody Charge u) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setDpd(u.getDpd());
              existing.setChargeAmount(u.getChargeAmount());
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
