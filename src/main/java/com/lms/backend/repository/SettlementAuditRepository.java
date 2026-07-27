package com.lms.backend.repository;

import com.lms.backend.entity.SettlementAudit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for SettlementAuditRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface SettlementAuditRepository extends JpaRepository<SettlementAudit, Long> {
  java.util.List<SettlementAudit> findByLoanAccount_Lan(Long lan);
  java.util.List<SettlementAudit> findByLoanCredit_CredId(Long credId);
}
