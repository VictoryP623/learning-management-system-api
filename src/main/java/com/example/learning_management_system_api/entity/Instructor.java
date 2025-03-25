package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import java.util.Set;
import lombok.Data;

@Entity
@Data
@Table(name = "instructor")
public class Instructor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Follow> followers;
}
