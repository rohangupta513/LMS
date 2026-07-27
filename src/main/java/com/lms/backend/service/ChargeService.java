package com.lms.backend.service;

import com.lms.backend.entity.Charge;
import com.lms.backend.repository.ChargeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
/**
 * Service class encapsulating business logic for ChargeService operations.
 * Interfaces with repositories to perform database transactions.
 */
public class ChargeService {
  @Autowired private ChargeRepository repo;

  public Charge add(Charge u) {
    return repo.save(u);
  }

  public List<Charge> getAll() {
    return repo.findAll();
  }

  public Optional<Charge> get(Long id) {
    return repo.findById(id);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }
}
