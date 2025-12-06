package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.utils.enums.LessonUnlockType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "lesson")
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Tên bài học
  private String name;

  @Size(max = 200000)
  private String description;

  // Bài học free xem thử hay không
  @Column(name = "is_free")
  private Boolean isFree = false;

  // Video HLS chính của bài học
  // TÁI SỬ DỤNG cột resource_url cũ để không phải đổi DB
  @Column(name = "resource_url")
  private String videoUrl;

  // Tổng thời lượng video (giây) – dùng để tính % xem / auto complete
  @Column(name = "duration_sec")
  private Integer durationSec;

  // Thứ tự bài trong course (dùng cho auto-play)
  @Column(name = "order_index")
  private Integer orderIndex;

  // Luật mở khóa bài học
  @Enumerated(EnumType.STRING)
  @Column(name = "unlock_type")
  private LessonUnlockType unlockType = LessonUnlockType.NONE;

  // Nếu SPECIFIC_LESSON_COMPLETED → phải hoàn thành bài này trước
  @Column(name = "required_lesson_id")
  private Long requiredLessonId;

  @ManyToOne
  @JoinColumn(name = "course_id", referencedColumnName = "id")
  private Course course;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
