package com.lms.backend.controller;

import com.lms.backend.dto.ChargeResponse;
import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.entity.Charge;
import com.lms.backend.service.ChargeService;
import java.util.List;
import java.util.stream.Collectors;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(400, e.getMessage()));
  }

  @PostMapping
  public ResponseEntity<ChargeResponse> add(@RequestBody Charge u) {
    return ResponseEntity.ok(ChargeResponse.fromEntity(service.add(u)));
  }

  @GetMapping
  public ResponseEntity<List<ChargeResponse>> getAll() {
    return ResponseEntity.ok(service.getAll().stream().map(ChargeResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ChargeResponse> get(@PathVariable Long id) {
    return service.get(id).map(c -> ResponseEntity.ok(ChargeResponse.fromEntity(c))).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ChargeResponse> update(@PathVariable Long id, @RequestBody Charge u) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setDpd(u.getDpd());
              existing.setChargeAmount(u.getChargeAmount());
              return ResponseEntity.ok(ChargeResponse.fromEntity(service.add(existing)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
