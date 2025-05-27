package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.LessonCompletion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
  boolean existsByStudentIdAndLessonId(Long studentId, Long lessonId);

  long countByStudentIdAndLesson_Course_Id(Long studentId, Long courseId);

  List<LessonCompletion> findByStudentIdAndLesson_Course_Id(Long studentId, Long courseId);
}
