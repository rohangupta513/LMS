package com.lms.backend.repository;

import com.lms.backend.entity.LanCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanChargeRepository extends JpaRepository<LanCharge, Long> {
}
