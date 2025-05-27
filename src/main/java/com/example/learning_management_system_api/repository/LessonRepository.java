package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Lesson;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
  boolean existsByNameAndCourseId(String name, Long courseId);

  List<Lesson> findByCourse_IdAndNameContaining(Long courseId, String name);

  boolean existsByNameAndCourseIdAndIdNot(String name, Long courseId, Long id);

  long countByCourseId(Long courseId);
  
  List<Lesson> findByCourse_Id(Long courseId);
}
