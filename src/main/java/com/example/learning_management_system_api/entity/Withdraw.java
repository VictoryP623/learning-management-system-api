package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "withdraw")
public class Withdraw {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long instructorId;
  public Double amount;
}
