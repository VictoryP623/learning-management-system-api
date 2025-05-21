package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.dto.response.EarningDTO;
import com.example.learning_management_system_api.entity.Course;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
    extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
  @Query(
      "SELECT new com.example.learning_management_system_api.dto.response.EarningDTO(c.id,"
          + " c.instructor.id, c.price * COUNT(p.student.id)) \n"
          + "FROM Course c JOIN Purchase p ON c.id = p.id \n"
          + "WHERE c.instructor.id = ?1 AND (p.createdAt >= ?2 AND p.createdAt <= ?3) \n"
          + "GROUP BY c.id\n")
  List<EarningDTO> getEarnings(Long id, LocalDateTime from, LocalDateTime to);

  boolean existsById(Long id);

  List<Course> findByCategoryId(Long id);

  Page<Course> findByStatus(String status, Pageable pageable);

  Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

  Page<Course> findByInstructorIdAndStatus(Long instructorId, String status, Pageable pageable);

  Page<Course> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);
}
