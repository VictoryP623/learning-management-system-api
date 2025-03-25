package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "admin")
public class Admin {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;
}
