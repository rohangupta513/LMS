package com.lms.backend.service;

import com.lms.backend.entity.Lender;
import com.lms.backend.repository.LenderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class LenderService {
  @Autowired private LenderRepository repo;
  @Autowired private com.lms.backend.repository.LoanAccountRepository loanAccountRepo;

  public Lender add(Lender u) {
    return repo.save(u);
  }

  public List<Lender> getAll() {
    return repo.findAll();
  }

  public Optional<Lender> get(Long id) {
    return repo.findById(id);
  }

  public void delete(Long id) {
    if (loanAccountRepo.countByLender_LenderId(id) > 0) {
      throw new IllegalStateException("Cannot delete lender: Lender has associated loan accounts.");
    }
    repo.deleteById(id);
  }
}
