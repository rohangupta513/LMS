package com.lms.backend.repository;

import com.lms.backend.entity.LoanAccount;
import com.lms.backend.enums.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LoanAccountRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
  long countByUser_UserId(Long userId);

  @Query("SELECT COUNT(l) FROM LoanAccount l WHERE l.user.userId = :userId AND l.status NOT IN :excludedStatuses")
  long countActiveLoansByUserId(@Param("userId") Long userId, @Param("excludedStatuses") List<LoanStatus> excludedStatuses);
}
