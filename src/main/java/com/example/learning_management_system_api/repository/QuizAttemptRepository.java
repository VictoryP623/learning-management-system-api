package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.QuizAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
  List<QuizAttempt> findByUserId(Long userId);

  @Modifying
  @Transactional
  @Query(
      "DELETE FROM QuizAttempt qa WHERE qa.quiz.id IN (SELECT q.id FROM Quiz q WHERE q.lesson.id ="
          + " :lessonId)")
  void deleteByLessonId(@org.springframework.data.repository.query.Param("lessonId") Long lessonId);
}
