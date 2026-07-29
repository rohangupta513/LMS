package com.lms.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

  @NotBlank(message = "Username cannot be blank")
  private String userName;
  @NotBlank(message = "User address cannot be blank")
  private String userAddress;
  @NotBlank(message = "User phone cannot be blank")
  @Column(unique = true)
  private String userPhone;
  @NotBlank(message = "User KYC details cannot be blank")
  private String userKycDetails;
}
