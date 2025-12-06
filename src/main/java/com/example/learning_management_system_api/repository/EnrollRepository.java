package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Enroll;
import com.example.learning_management_system_api.entity.Id.EnrollId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollRepository extends JpaRepository<Enroll, EnrollId> {
  Page<Enroll> findByCourseId(Long courseId, Pageable pageable);

  Optional<Page<Enroll>> findByStudentId(Long studentId, Pageable pageable);

  boolean existsById(EnrollId id);

  @Query(
      "select e.student.user.id from Enroll e where e.course.id = :courseId and e.student.user.id"
          + " is not null")
  List<Long> findStudentUserIdsByCourseId(@Param("courseId") Long courseId);
}
