package com.lms.backend.repository;

import com.lms.backend.entity.Lender;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for LenderRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface LenderRepository extends JpaRepository<Lender, Long> {}
