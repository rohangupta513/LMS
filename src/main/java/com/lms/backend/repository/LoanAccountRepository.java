package com.lms.backend.repository;

import com.lms.backend.entity.LoanAccount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LoanAccountRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
  long countByUser_UserId(Long userId);
}
