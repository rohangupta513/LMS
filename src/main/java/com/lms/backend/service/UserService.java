package com.lms.backend.service;

import com.lms.backend.entity.User;
import com.lms.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service

public class UserService {
  @Autowired private UserRepository repo;
  @Autowired private com.lms.backend.repository.LoanAccountRepository loanAccountRepo;

  @Caching(evict = { 
    @CacheEvict(value = "user", key = "#result.userId", condition = "#result != null && #result.userId != null"), 
    @CacheEvict(value = "users", allEntries = true) 
  })
  public User add(User u) {
    Optional<User> existingUser = repo.findByUserPhone(u.getUserPhone());
    if (existingUser.isPresent() && (u.getUserId() == null || !existingUser.get().getUserId().equals(u.getUserId()))) {
        throw new IllegalArgumentException("User with this phone number already exists.");
    }
    return repo.save(u);
  }

  @Cacheable(value = "users")
  public List<User> getAll() {
    return repo.findAll();
  }

  @Cacheable(value = "user", key = "#id")
  public Optional<User> get(Long id) {
    return repo.findById(id);
  }

  @Caching(evict = { 
    @CacheEvict(value = "user", key = "#id"), 
    @CacheEvict(value = "users", allEntries = true) 
  })
  public void delete(Long id) {
    if (loanAccountRepo.countByUser_UserId(id) > 0) {
      throw new IllegalStateException("Cannot delete user: User has associated loan accounts.");
    }
    repo.deleteById(id);
  }
}
