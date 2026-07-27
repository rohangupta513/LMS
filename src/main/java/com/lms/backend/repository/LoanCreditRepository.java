package com.lms.backend.repository;

import com.lms.backend.entity.LoanCredit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LoanCreditRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LoanCreditRepository extends JpaRepository<LoanCredit, Long> {
  java.util.List<LoanCredit> findByLoanAccount_Lan(Long lan);
}
