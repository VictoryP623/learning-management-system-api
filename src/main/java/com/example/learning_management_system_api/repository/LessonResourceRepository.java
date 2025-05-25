package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.LessonResource;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonResourceRepository extends JpaRepository<LessonResource, Long> {
  Page<LessonResource> findByLessonId(Long lessonId, Pageable pageable);

  boolean existsByNameAndLessonId(String name, Long lessonId);

  //function này để lấy list resource cho 1 lesson (dùng cho service)
  List<LessonResource> findByLessonId(Long lessonId);
}
