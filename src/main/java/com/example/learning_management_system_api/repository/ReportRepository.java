package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Id.ReportId;
import com.example.learning_management_system_api.entity.Report;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, ReportId> {

  List<Report> findByCourseId(Long courseId);
}
