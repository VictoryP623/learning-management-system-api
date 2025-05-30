package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

  Page<Category> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

  boolean existsByName(String name);
}
