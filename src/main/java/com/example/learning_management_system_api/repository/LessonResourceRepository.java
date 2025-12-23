package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.LessonResource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {

  // Paging
  Page<LessonResource> findByLesson_Id(Long lessonId, Pageable pageable);

  // List (sort theo orderIndex)
  List<LessonResource> findByLesson_IdOrderByOrderIndexAsc(Long lessonId);

  // Replace theo name (ignore case)
  Optional<LessonResource> findFirstByLesson_IdAndNameIgnoreCase(Long lessonId, String name);

  boolean existsByNameIgnoreCaseAndLesson_Id(String name, Long lessonId);

  @Query(
      "select coalesce(max(lr.orderIndex), 0) from LessonResource lr where lr.lesson.id ="
          + " :lessonId")
  Integer findMaxOrderIndexByLessonId(@Param("lessonId") Long lessonId);
}
