package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "purchase")
public class Purchase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Boolean isPaid;

  private Double totalAmount;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToMany
  @JoinTable(
      name = "purchase_item",
      joinColumns = @JoinColumn(name = "purchase_id"),
      inverseJoinColumns = @JoinColumn(name = "course_id"))
  private Set<Course> courses;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
