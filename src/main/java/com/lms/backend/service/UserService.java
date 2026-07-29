package com.lms.backend.service;

import com.lms.backend.entity.User;
import com.lms.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/**
 * Service class encapsulating business logic for UserService operations.
 * Interfaces with repositories to perform database transactions.
 */
public class UserService {
  @Autowired private UserRepository repo;

  public User add(User u) {
    Optional<User> existingUser = repo.findByUserPhone(u.getUserPhone());
    if (existingUser.isPresent() && (u.getUserId() == null || !existingUser.get().getUserId().equals(u.getUserId()))) {
        throw new IllegalArgumentException("User with this phone number already exists.");
    }
    return repo.save(u);
  }

  public List<User> getAll() {
    return repo.findAll();
  }

  public Optional<User> get(Long id) {
    return repo.findById(id);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }
}
