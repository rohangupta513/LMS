package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lenders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the Lender database table.
 * Contains the data model and structural mappings.
 */
public class Lender {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long lenderId;

  private String lenderName;
  private String lenderContact;
  private String lenderDetails;
}
