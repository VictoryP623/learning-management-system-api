package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.entity.Id.EnrollId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "enroll")
public class Enroll {

  @EmbeddedId EnrollId id;

  @ManyToOne
  @MapsId("studentId")
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne
  @MapsId("courseId")
  @JoinColumn(name = "course_id")
  private Course course;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
