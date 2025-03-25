package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Instructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
  Instructor getInstructorById(Long id);

  Instructor findByUserId(Long id);

  Page<Instructor> findByUserFullnameContainingIgnoreCase(String fullname, Pageable pageable);
}
