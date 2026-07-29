package com.lms.backend.repository;

import com.lms.backend.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  java.util.Optional<User> findByUserPhone(String userPhone);
}
