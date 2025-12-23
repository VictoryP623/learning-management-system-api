package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.QuizAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

  // Sửa lại cho đúng field: user.id
  List<QuizAttempt> findByUser_Id(Long userId);

  @Modifying
  @Transactional
  @Query(
      "DELETE FROM QuizAttempt qa WHERE qa.quiz.id IN (SELECT q.id FROM Quiz q WHERE q.lesson.id ="
          + " :lessonId)")
  void deleteByLessonId(@Param("lessonId") Long lessonId);

  // Số quiz khác nhau trong course mà user đã làm (có ít nhất 1 attempt)
  @Query(
      "select count(distinct qa.quiz.id) "
          + "from QuizAttempt qa "
          + "where qa.user.id = :userId "
          + "and qa.quiz.lesson.course.id = :courseId")
  long countDistinctQuizAttemptedByUserAndCourse(
      @Param("userId") Long userId, @Param("courseId") Long courseId);
}
