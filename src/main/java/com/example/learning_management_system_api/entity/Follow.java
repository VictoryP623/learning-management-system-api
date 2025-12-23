package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.entity.Id.FollowId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "follow")
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

  @EmbeddedId private FollowId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("studentId")
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("instructorId")
  @JoinColumn(name = "instructor_id")
  private Instructor instructor;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
