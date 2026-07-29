package com.lms.backend.service;

import com.lms.backend.entity.Loan;
import com.lms.backend.repository.LoanRepository;
import com.lms.backend.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/**
 * Service class encapsulating business logic for LoanService operations.
 * Interfaces with repositories to perform database transactions.
 */
public class LoanService {
  @Autowired private LoanRepository repo;
  @Autowired private com.lms.backend.repository.LoanAccountRepository loanAccountRepo;

  public Loan add(Loan u) {
    return repo.save(u);
  }

  public Loan update(Long id, Loan u) {
    return repo.findById(id).map(existing -> {
      existing.setLoanAmountMin(u.getLoanAmountMin());
      existing.setLoanAmountMax(u.getLoanAmountMax());
      existing.setLoanInterestMin(u.getLoanInterestMin());
      existing.setLoanInterestMax(u.getLoanInterestMax());
      existing.setLoanTimeMin(u.getLoanTimeMin());
      existing.setLoanTimeMax(u.getLoanTimeMax());
      existing.setTypeOfLoan(u.getTypeOfLoan());
      return repo.save(existing);
    }).orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
  }

  public List<Loan> getAll() {
    return repo.findAll();
  }

  public Optional<Loan> get(Long id) {
    return repo.findById(id);
  }

  public void delete(Long id) {
    if (loanAccountRepo.countByLoan_LoanId(id) > 0) {
      throw new IllegalStateException("Cannot delete loan configuration: Loan has associated loan accounts.");
    }
    repo.deleteById(id);
  }
}
