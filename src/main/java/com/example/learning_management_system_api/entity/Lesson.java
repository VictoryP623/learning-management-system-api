package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "lesson")
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Size(max = 200000)
  private String description;

  @Column(name = "is_free")
  private Boolean isFree = false;

  @Column(name = "resource_url")
  private String resourceUrl;

  @ManyToOne
  @JoinColumn(name = "course_id", referencedColumnName = "id")
  private Course course;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
