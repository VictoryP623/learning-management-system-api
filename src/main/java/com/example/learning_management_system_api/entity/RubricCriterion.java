package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignment_rubric_criterion")
public class RubricCriterion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rubric_id", nullable = false)
  private Rubric rubric;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "max_score", nullable = false)
  private Integer maxScore;

  @Column(name = "order_index")
  private Integer orderIndex;
}
