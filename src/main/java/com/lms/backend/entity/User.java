package com.lms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * JPA Entity representing the User database table.
 * Contains the data model and structural mappings.
 */
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  private String userName;
  private String userAddress;
  private String userPhone;
  private String userKycDetails;
}
