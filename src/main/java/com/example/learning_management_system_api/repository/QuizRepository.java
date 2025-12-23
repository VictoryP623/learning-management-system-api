package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

  Page<Quiz> findByLesson_Id(Long lessonId, Pageable pageable);

  @Modifying
  @Transactional
  @Query("DELETE FROM Quiz q WHERE q.lesson.id = :lessonId")
  void deleteByLessonId(@Param("lessonId") Long lessonId);

  // Đếm tổng số quiz thuộc 1 course
  long countByLesson_Course_Id(Long courseId);
}
