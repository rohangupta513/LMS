package com.lms.backend.repository;

import com.lms.backend.entity.Loan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LoanRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {
  @Query(
      "SELECT l FROM Loan l WHERE "
          + "l.loanAmountMin <= :amount AND l.loanAmountMax >= :amount AND "
          + "l.loanInterestMin <= :interest AND l.loanInterestMax >= :interest AND "
          + "l.loanTimeMin <= :time AND l.loanTimeMax >= :time AND "
          + "l.typeOfLoan = :type")
  List<Loan> findByCriteria(
      @Param("amount") Double amount,
      @Param("interest") Double interest,
      @Param("time") Integer time,
      @Param("type") com.lms.backend.enums.LoanType type);
}
