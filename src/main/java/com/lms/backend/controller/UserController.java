package com.lms.backend.controller;

import com.lms.backend.entity.User;
import com.lms.backend.service.UserService;
import java.util.List;
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

  @PostMapping
  public ResponseEntity<User> add(@RequestBody User u) {
    return ResponseEntity.ok(service.add(u));
  }

  @GetMapping
  public ResponseEntity<List<User>> getAll() {
    return ResponseEntity.ok(service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> get(@PathVariable Long id) {
    return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User u) {
    return service
        .get(id)
        .map(
            existing -> {
              existing.setUserName(u.getUserName());
              existing.setUserAddress(u.getUserAddress());
              existing.setUserPhone(u.getUserPhone());
              existing.setUserKycDetails(u.getUserKycDetails());
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
