package com.lms.backend.repository;

import com.lms.backend.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Spring Data JPA Repository interface for UserRepository entities.
 * Provides abstraction for direct database interactions.
 */
public interface UserRepository extends JpaRepository<User, Long> {}
