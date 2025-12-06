package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "lesson_resource")
public class LessonResource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Tên hiển thị "Slide chương 1", "Tài liệu PDF"
  private String name;

  // Link file
  private String url;

  // Loại file: PDF, PPT, DOCX, IMAGE, OTHER
  @Column(name = "type")
  private String type;

  // Thứ tự hiển thị trong list tài liệu
  @Column(name = "order_index")
  private Integer orderIndex;

  @ManyToOne
  @JoinColumn(name = "lesson_id", referencedColumnName = "id")
  private Lesson lesson;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
