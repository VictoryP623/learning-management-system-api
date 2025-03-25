package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Id.SavedCourseId;
import com.example.learning_management_system_api.entity.SavedCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedCourseRepository extends JpaRepository<SavedCourse, SavedCourseId> {

  Page<SavedCourse> findByStudentId(Long studentId, Pageable pageable);
}
