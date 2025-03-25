package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.utils.enums.QuizType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Data
public class Quiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String question;

  @ManyToOne
  @JoinColumn(name = "lesson_id", referencedColumnName = "id")
  private Lesson lesson;

  @Enumerated(EnumType.STRING)
  private QuizType quizType;

  @ElementCollection
  @CollectionTable(name = "quiz_answers", joinColumns = @JoinColumn(name = "quiz_id"))
  private List<AnswerOption> answerOptions = new ArrayList<>();
}
