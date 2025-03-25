package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.dto.request.CourseDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@NoArgsConstructor
@Table(name = "course")
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "instructor_id", referencedColumnName = "id")
  private Instructor instructor;

  private Double price;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;

  private String thumbnail;

  private String status;

  private String name;

  @ManyToOne
  @JoinColumn(name = "category_id", referencedColumnName = "id")
  private Category category;

  @OneToMany(mappedBy = "course")
  Set<Enroll> enrolls;

  @ManyToMany(mappedBy = "courses")
  Set<Purchase> purchases;

  public Course(CourseDTO courseDTO) {
    this.id = courseDTO.getId();
    this.price = courseDTO.getPrice();
    this.thumbnail = courseDTO.getThumbnail();
    this.name = courseDTO.getName();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Course course = (Course) obj;
    return Objects.equals(id, course.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Course{" + "id=" + id + ", name='" + name + '\'' + '}';
  }
}
