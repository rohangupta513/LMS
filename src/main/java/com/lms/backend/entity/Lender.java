package com.lms.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "lenders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lender {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long lenderId;

  @NotBlank(message = "Lender name cannot be blank")
  private String lenderName;
  @NotBlank(message = "Lender contact cannot be blank")
  private String lenderContact;
  @NotBlank(message = "Lender details cannot be blank")
  private String lenderDetails;
}
