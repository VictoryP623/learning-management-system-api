package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.entity.Id.ReportId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "report")
public class Report {
  @EmbeddedId ReportId id;

  @ManyToOne
  @MapsId("studentId")
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne
  @MapsId("courseId")
  @JoinColumn(name = "course_id")
  private Course course;

  private String description;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
