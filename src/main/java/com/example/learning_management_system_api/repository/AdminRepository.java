package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {}
