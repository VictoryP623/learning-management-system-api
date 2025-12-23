package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Assignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

  List<Assignment> findByLesson_Id(Long lessonId);

  List<Assignment> findByLesson_Course_Id(Long courseId);

  // Đếm tổng số assignment thuộc 1 course
  long countByLesson_Course_Id(Long courseId);
}
