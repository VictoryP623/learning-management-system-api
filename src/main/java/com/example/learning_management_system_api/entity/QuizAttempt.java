package com.example.learning_management_system_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class QuizAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "quiz_id", referencedColumnName = "id")
  @JsonIgnore
  private Quiz quiz;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  @JsonIgnore
  private User user;

  private Integer answerId;

  private boolean isCorrect;

  private LocalDateTime attemptTimestamp;
}
