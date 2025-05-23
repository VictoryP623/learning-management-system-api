package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.entity.SavedCourse;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.service.ISaveCourseService;
import com.example.learning_management_system_api.service.IStudentService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-courses")
//@CrossOrigin(origins = "http://localhost:3000")
public class SavedCourseController {

    @Autowired
    private ISaveCourseService savedCourseService;

    @Autowired
    private IStudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_Student')")
    public ResponseVO<?> saveCourse(
            @Valid @RequestBody SavedCourse savedCourse,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long id = userDetails.getUserId();

        // Lấy thông tin Student dựa trên email
        Optional<Student> student = studentService.getStudentById(id);
        if (student.isEmpty()) {
            return ResponseVO.error(400, "Student not found for the logged-in user.");
        }

        // Gán studentId vào đối tượng SavedCourse
        savedCourse.setStudentId(student.get().getId());


            // Lưu khóa học
            savedCourseService.saveCourse(savedCourse);


        return ResponseVO.success("Course saved successfully!");
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_Student')")
    public ResponseEntity<?> deleteCourse(
            @RequestParam Long courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long id = userDetails.getUserId();
            Optional<Student> student = studentService.getStudentById(id);
            if (student.isEmpty()) {
        return ResponseEntity.badRequest().body("Student not found for the logged-in user.");
            }
            // Tạo đối tượng SavedCourse và gán thông tin
            SavedCourse savedCourse = new SavedCourse();
            savedCourse.setStudentId(student.get().getId());
            savedCourse.setCourseId(courseId); // Gán courseId vào đối tượng SavedCourse

            // Thực hiện xóa khóa học
            savedCourseService.deleteCourse(savedCourse);
            return ResponseEntity.ok("Course deleted successfully!");
        } catch (Exception e) {
      return ResponseEntity.badRequest().body("Failed to delete the course: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_Student')")
    public ResponseEntity<?> getSavedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long id = userDetails.getUserId();

            Optional<Student> student = studentService.getStudentById(id);
            if (student.isEmpty()) {
        return ResponseEntity.badRequest().body("Student not found for the logged-in user.");
            }

            Pageable pageable = PageRequest.of(page, size);
            PageDto savedCourses =
                    savedCourseService.getSavedCoursesByStudentId(student.get().getId(), pageable);
            return ResponseEntity.ok(savedCourses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to retrieve saved courses: " + e.getMessage());
        }
    }
}