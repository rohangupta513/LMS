package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the Charge database table.
 * Contains the data model and structural mappings.
 */
public class Charge {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long chargeId;

  private Integer dpd;
  private Double chargeAmount;
}
