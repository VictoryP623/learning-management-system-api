package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.LessonCompletion;
import com.example.learning_management_system_api.utils.enums.LessonProgressStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {

  boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

  long countByStudentIdAndLesson_Course_Id(Long studentId, Long courseId);

  List<LessonCompletion> findByStudentIdAndLesson_Course_Id(Long studentId, Long courseId);

  LessonCompletion findByStudentIdAndLessonId(Long studentId, Long lessonId);

  boolean existsByStudentIdAndLessonIdAndStatus(
      Long studentId, Long lessonId, LessonProgressStatus status);

  // Đếm số lesson đã hoàn thành trong 1 course
  long countByStudentIdAndLesson_Course_IdAndStatus(
      Long studentId, Long courseId, LessonProgressStatus status);
}
