package com.lms.backend.repository;

import com.lms.backend.entity.RepaymentScheduler;
import com.lms.backend.enums.RepaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for RepaymentSchedulerRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface RepaymentSchedulerRepository extends JpaRepository<RepaymentScheduler, Long> {
  List<RepaymentScheduler> findByLoanAccount_LanAndStatusOrderByDueDateAsc(
      Long lan, RepaymentStatus status);

  List<RepaymentScheduler> findByLoanAccount_LanOrderByDueDateAsc(Long lan);
}
