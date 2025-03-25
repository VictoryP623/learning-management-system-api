package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.entity.Id.SavedCourseId;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "saved_course")
@IdClass(SavedCourseId.class)
public class SavedCourse {

  @Id
  @Column(name = "student_id", insertable = false, updatable = false)
  private Long studentId;

  @Id
  @NotNull(message = "Course ID cannot be null")
  @Column(name = "course_id", insertable = false, updatable = false)
  private Long courseId;

  @ManyToOne
  @JoinColumn(
      name = "student_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(
      name = "course_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false)
  private Course course;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;
}
