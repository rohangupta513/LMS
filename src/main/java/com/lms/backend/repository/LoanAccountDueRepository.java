package com.lms.backend.repository;

import com.lms.backend.entity.LoanAccountDue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LoanAccountDueRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LoanAccountDueRepository extends JpaRepository<LoanAccountDue, Long> {}
