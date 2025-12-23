package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.response.*;
import com.example.learning_management_system_api.entity.*;
import com.example.learning_management_system_api.exception.NotFoundException;
import com.example.learning_management_system_api.repository.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentAssignmentTimelineService {

  private final LessonRepository lessonRepository;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;

  private final StudentRepository studentRepository;
  private final EnrollRepository enrollRepository;

  @Transactional(readOnly = true)
  public StudentAssignmentTimelineDto getTimelineByCourse(Long courseId, Long userId) {

    // 1) userId -> student
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Student not found for userId=" + userId));

    // 2) verify enrolled
    boolean enrolled = enrollRepository.existsByStudent_IdAndCourse_Id(student.getId(), courseId);
    if (!enrolled) {
      // bạn có thể đổi sang ConflictException/BadRequestException tùy style
      throw new NotFoundException("You are not enrolled in this course");
    }

    // 3) load lessons of course
    List<Lesson> lessons = lessonRepository.findByCourse_Id(courseId);

    // 4) load assignments of course
    List<Assignment> assignments = assignmentRepository.findByLesson_Course_Id(courseId);

    // 5) load submissions in course (1 query)
    List<AssignmentSubmission> submissions =
        assignmentSubmissionRepository.findByStudent_IdAndAssignment_Lesson_Course_Id(
            student.getId(), courseId);

    Map<Long, AssignmentSubmission> submissionByAssignmentId =
        submissions.stream()
            .collect(
                Collectors.toMap(s -> s.getAssignment().getId(), Function.identity(), (a, b) -> a));

    // 6) group assignments by lessonId
    Map<Long, List<Assignment>> assignmentsByLessonId =
        assignments.stream().collect(Collectors.groupingBy(a -> a.getLesson().getId()));

    // 7) build response
    List<StudentAssignmentLessonDto> lessonDtos =
        lessons.stream()
            .map(
                lesson -> {
                  List<Assignment> list =
                      assignmentsByLessonId.getOrDefault(lesson.getId(), List.of());

                  List<StudentAssignmentItemDto> items =
                      list.stream()
                          .map(a -> toItemDto(a, submissionByAssignmentId.get(a.getId())))
                          .sorted(
                              Comparator.comparing(
                                  StudentAssignmentItemDto::getDueAt,
                                  Comparator.nullsLast(Comparator.naturalOrder())))
                          .toList();

                  return StudentAssignmentLessonDto.builder()
                      .lessonId(lesson.getId())
                      .lessonName(lesson.getName())
                      .assignments(items)
                      .build();
                })
            .toList();

    return StudentAssignmentTimelineDto.builder().courseId(courseId).lessons(lessonDtos).build();
  }

  private StudentAssignmentItemDto toItemDto(Assignment a, AssignmentSubmission s) {

    LocalDateTime dueAt = a.getDueAt();
    Integer maxScore = a.getMaxScore();

    String status;
    LocalDateTime submittedAt = null;
    Integer score = null;
    String feedback = null;
    boolean late = false;

    if (s == null) {
      status = "NOT_SUBMITTED";
    } else {
      submittedAt = s.getSubmittedAt();
      score = s.getScore();
      feedback = s.getFeedback();

      if (submittedAt != null && dueAt != null) {
        late = submittedAt.isAfter(dueAt);
      }

      status = (score == null) ? "SUBMITTED" : "GRADED";
    }

    return StudentAssignmentItemDto.builder()
        .assignmentId(a.getId())
        .title(a.getTitle())
        .dueAt(dueAt)
        .maxScore(maxScore)
        .status(status)
        .submittedAt(submittedAt)
        .score(score)
        .feedback(feedback)
        .late(late)
        .build();
  }
}
