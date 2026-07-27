package com.lms.backend.repository;

import com.lms.backend.entity.Charge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for ChargeRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface ChargeRepository extends JpaRepository<Charge, Long> {}
