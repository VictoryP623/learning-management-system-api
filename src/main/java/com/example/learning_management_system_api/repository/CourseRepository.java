package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Course;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
    extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
  @Query(
      value =
          "SELECT c.id AS courseId, c.instructor_id AS instructorId, c.name AS courseName,"
              + " COUNT(pi.course_id) AS soldCount, IFNULL(COUNT(pi.course_id) * c.price, 0) AS"
              + " revenue FROM course c LEFT JOIN purchase_item pi ON pi.course_id = c.id LEFT JOIN"
              + " purchase p ON pi.purchase_id = p.id AND p.is_paid = 1 WHERE c.instructor_id ="
              + " :instructorId GROUP BY c.id, c.instructor_id, c.name, c.price",
      nativeQuery = true)
  List<Object[]> getEarningsNative(@Param("instructorId") Long instructorId);

  boolean existsById(Long id);

  List<Course> findByCategoryId(Long id);

  Page<Course> findByStatus(String status, Pageable pageable);

  Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

  Page<Course> findByInstructorIdAndStatus(Long instructorId, String status, Pageable pageable);

  Page<Course> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);
}
