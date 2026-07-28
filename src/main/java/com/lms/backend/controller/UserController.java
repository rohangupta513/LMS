package com.lms.backend.controller;

import com.lms.backend.dto.ErrorResponse;
import com.lms.backend.dto.UserResponse;
import com.lms.backend.entity.User;
import com.lms.backend.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
/**
 * REST Controller handling incoming HTTP requests for UserController entities.
 * Exposes endpoints for CRUD operations and business logic flows.
 */
public class UserController {
  @Autowired private UserService service;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity.badRequest().body(ErrorResponse.of(400, e.getMessage()));
  }

  @PostMapping
  public ResponseEntity<UserResponse> add(@RequestBody User u) {
    return ResponseEntity.ok(UserResponse.fromEntity(service.add(u)));
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAll() {
    return ResponseEntity.ok(service.getAll().stream().map(UserResponse::fromEntity).collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> get(@PathVariable Long id) {
    return service.get(id).map(u -> ResponseEntity.ok(UserResponse.fromEntity(u))).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody User u) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setUserName(u.getUserName());
              existing.setUserAddress(u.getUserAddress());
              existing.setUserPhone(u.getUserPhone());
              existing.setUserKycDetails(u.getUserKycDetails());
              return ResponseEntity.ok(UserResponse.fromEntity(service.add(existing)));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.ok().build();
  }
}
