package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.response.StudentProgressDto;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Enroll;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.AssignmentRepository;
import com.example.learning_management_system_api.repository.AssignmentSubmissionRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.EnrollRepository;
import com.example.learning_management_system_api.repository.LessonCompletionRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.QuizAttemptRepository;
import com.example.learning_management_system_api.repository.QuizRepository;
import com.example.learning_management_system_api.utils.enums.LessonProgressStatus;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstructorCourseStudentService {

  private final CourseRepository courseRepository;
  private final EnrollRepository enrollRepository;
  private final LessonRepository lessonRepository;
  private final LessonCompletionRepository lessonCompletionRepository;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final QuizRepository quizRepository;
  private final QuizAttemptRepository quizAttemptRepository;

  /**
   * Lấy danh sách học viên đã enroll + tiến độ học trong một khoá, chỉ cho phép Instructor sở hữu
   * khoá đó xem.
   */
  public List<StudentProgressDto> getStudentsProgress(Long courseId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails customUser)) {
      throw new AppException(401, "Unauthorized");
    }
    Long currentUserId = customUser.getUserId();

    // Lấy course và check instructor
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new AppException(404, "Course not found"));

    if (course.getInstructor() == null
        || course.getInstructor().getUser() == null
        || !course.getInstructor().getUser().getId().equals(currentUserId)) {
      throw new AppException(403, "You are not the instructor of this course");
    }

    long totalLessons = lessonRepository.countByCourseId(courseId);
    long totalAssignments = assignmentRepository.countByLesson_Course_Id(courseId);
    long totalQuizzes = quizRepository.countByLesson_Course_Id(courseId);

    long totalItems = totalLessons + totalAssignments + totalQuizzes;
    long safeTotalItems = (totalItems == 0) ? 1 : totalItems; // không gán lại totalItems

    // Lấy danh sách enroll (Student + User) của khoá
    List<Enroll> enrolls = enrollRepository.findByCourse_Id(courseId);

    return enrolls.stream()
        .map(
            enroll ->
                buildStudentProgress(
                    enroll,
                    courseId,
                    totalLessons,
                    totalAssignments,
                    totalQuizzes,
                    safeTotalItems) // dùng safeTotalItems
            )
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private StudentProgressDto buildStudentProgress(
      Enroll enroll,
      Long courseId,
      long totalLessons,
      long totalAssignments,
      long totalQuizzes,
      long totalItems) {

    Student student = enroll.getStudent();
    if (student == null || student.getUser() == null) {
      return null;
    }
    User user = student.getUser();

    long lessonsCompleted =
        (totalLessons == 0)
            ? 0
            : lessonCompletionRepository.countByStudentIdAndLesson_Course_IdAndStatus(
                student.getId(), courseId, LessonProgressStatus.COMPLETED);

    long assignmentsSubmitted =
        (totalAssignments == 0)
            ? 0
            : assignmentSubmissionRepository.countByStudent_IdAndAssignment_Lesson_Course_Id(
                student.getId(), courseId);

    Double avgAssignmentScore =
        assignmentSubmissionRepository.avgScoreByStudentAndCourse(student.getId(), courseId);

    long quizzesAttempted =
        (totalQuizzes == 0)
            ? 0
            : quizAttemptRepository.countDistinctQuizAttemptedByUserAndCourse(
                user.getId(), courseId);

    long completedItems = lessonsCompleted + assignmentsSubmitted + quizzesAttempted;
    double progressPercent = (completedItems * 100.0) / totalItems;

    return StudentProgressDto.builder()
        .studentId(student.getId())
        .fullname(user.getFullname())
        .email(user.getEmail())
        .lessonsCompleted(lessonsCompleted)
        .totalLessons(totalLessons)
        .assignmentsSubmitted(assignmentsSubmitted)
        .totalAssignments(totalAssignments)
        .avgAssignmentScore(avgAssignmentScore)
        .quizzesAttempted(quizzesAttempted)
        .totalQuizzes(totalQuizzes)
        .progressPercent(progressPercent)
        .build();
  }
}
