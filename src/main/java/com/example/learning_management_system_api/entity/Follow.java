package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.entity.Id.FollowId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "follow")
@IdClass(FollowId.class)
public class Follow {

  @Id
  @Column(name = "student_id")
  private Long studentId;

  @Id
  @Column(name = "instructor_id")
  private Long instructorId;

  @ManyToOne
  @JoinColumn(name = "student_id", insertable = false, updatable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "instructor_id", insertable = false, updatable = false)
  private Instructor instructor;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
