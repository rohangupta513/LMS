package com.lms.backend.controller;

import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.LenderResponse;
import com.lms.backend.entity.Lender;
import com.lms.backend.service.LenderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lenders")

public class LenderController {
  @Autowired private LenderService service;


  @PostMapping
  public ResponseEntity<LenderResponse> add(@Valid @RequestBody Lender l) {
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
              if (l.getLenderName() != null) existing.setLenderName(l.getLenderName());
              if (l.getLenderContact() != null) existing.setLenderContact(l.getLenderContact());
              if (l.getLenderDetails() != null) existing.setLenderDetails(l.getLenderDetails());
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
