package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(name = "student")
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @OneToMany(mappedBy = "student")
  Set<Enroll> enrolls;

  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Follow> follows;

  @OneToMany(mappedBy = "student")
  Set<Purchase> purchases;
}
