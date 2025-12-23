package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.AssignmentSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

  List<AssignmentSubmission> findByAssignment_Id(Long assignmentId);

  List<AssignmentSubmission> findByStudent_IdAndAssignment_Lesson_Id(Long studentId, Long lessonId);

  List<AssignmentSubmission> findByStudent_IdAndAssignment_Lesson_Course_Id(
      Long studentId, Long courseId);

  Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(
      Long assignmentId, Long studentId);

  boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);

  long countByStudent_IdAndAssignment_Lesson_Course_Id(Long studentId, Long courseId);

  @Query(
      "select avg(s.score) from AssignmentSubmission s "
          + "where s.student.id = :studentId "
          + "and s.assignment.lesson.course.id = :courseId "
          + "and s.score is not null")
  Double avgScoreByStudentAndCourse(
      @Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
