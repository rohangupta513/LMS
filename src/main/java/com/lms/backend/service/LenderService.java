package com.lms.backend.service;

import com.lms.backend.entity.Lender;
import com.lms.backend.repository.LenderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service

public class LenderService {
  @Autowired private LenderRepository repo;
  @Autowired private com.lms.backend.repository.LoanAccountRepository loanAccountRepo;

  @Caching(evict = { 
    @CacheEvict(value = "lender", key = "#result.lenderId", condition = "#result != null && #result.lenderId != null"), 
    @CacheEvict(value = "lenders", allEntries = true) 
  })
  public Lender add(Lender u) {
    return repo.save(u);
  }

  @Cacheable(value = "lenders")
  public List<Lender> getAll() {
    return repo.findAll();
  }

  @Cacheable(value = "lender", key = "#id")
  public Optional<Lender> get(Long id) {
    return repo.findById(id);
  }

  @Caching(evict = { 
    @CacheEvict(value = "lender", key = "#id"), 
    @CacheEvict(value = "lenders", allEntries = true) 
  })
  public void delete(Long id) {
    if (loanAccountRepo.countByLender_LenderId(id) > 0) {
      throw new IllegalStateException("Cannot delete lender: Lender has associated loan accounts.");
    }
    repo.deleteById(id);
  }
}
