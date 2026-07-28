package com.lms.backend.controller;

import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.LenderResponse;
import com.lms.backend.entity.Lender;
import com.lms.backend.service.LenderService;
import java.util.List;
import java.util.stream.Collectors;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(400, e.getMessage()));
  }

  @PostMapping
  public ResponseEntity<LenderResponse> add(@RequestBody Lender l) {
    return ResponseEntity.ok(LenderResponse.fromEntity(service.add(l)));
  }

  @GetMapping
  public ResponseEntity<List<LenderResponse>> getAll() {
    return ResponseEntity.ok(service.getAll().stream().map(LenderResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<LenderResponse> get(@PathVariable Long id) {
    return service.get(id).map(l -> ResponseEntity.ok(LenderResponse.fromEntity(l))).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<LenderResponse> update(@PathVariable Long id, @RequestBody Lender l) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setLenderName(l.getLenderName());
              existing.setLenderContact(l.getLenderContact());
              existing.setLenderDetails(l.getLenderDetails());
              return ResponseEntity.ok(LenderResponse.fromEntity(service.add(existing)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
