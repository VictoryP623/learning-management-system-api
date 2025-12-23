package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.AssignmentMapper;
import com.example.learning_management_system_api.dto.mapper.AssignmentSubmissionMapper;
import com.example.learning_management_system_api.dto.request.AssignmentRequestDto;
import com.example.learning_management_system_api.dto.request.AssignmentSubmissionRequestDto;
import com.example.learning_management_system_api.dto.request.GradeSubmissionRequestDto;
import com.example.learning_management_system_api.dto.response.AssignmentResponseDto;
import com.example.learning_management_system_api.dto.response.AssignmentSubmissionResponseDto;
import com.example.learning_management_system_api.entity.Assignment;
import com.example.learning_management_system_api.entity.AssignmentSubmission;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.AssignmentRepository;
import com.example.learning_management_system_api.repository.AssignmentSubmissionRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository submissionRepository;
  private final LessonRepository lessonRepository;
  private final StudentRepository studentRepository;
  private final AssignmentMapper assignmentMapper;
  private final AssignmentSubmissionMapper submissionMapper;
  private final EnrollService enrollService;

  // ================================================
  // Tạo Assignment
  // ================================================
  @Transactional
  public AssignmentResponseDto createAssignment(AssignmentRequestDto dto) {
    Lesson lesson =
        lessonRepository
            .findById(dto.lessonId())
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    // TODO: check instructor owns this course (giống checkPermission trong LessonService)

    Assignment assignment = new Assignment();
    assignment.setLesson(lesson);
    assignment.setTitle(dto.title());
    assignment.setDescription(dto.description());
    assignment.setDueAt(dto.dueAt());
    assignment.setMaxScore(dto.maxScore());

    return assignmentMapper.toDto(assignmentRepository.save(assignment));
  }

  // ================================================
  // Lấy chi tiết 1 Assignment
  // ================================================
  @Transactional(readOnly = true)
  public AssignmentResponseDto getAssignmentById(Long id) {
    Assignment assignment =
        assignmentRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Assignment not found"));

    return assignmentMapper.toDto(assignment);
  }

  // ================================================
  // Danh sách Assignment theo Lesson
  // ================================================
  @Transactional(readOnly = true)
  public List<AssignmentResponseDto> getAssignmentsByLesson(Long lessonId) {
    List<Assignment> list = assignmentRepository.findByLesson_Id(lessonId);
    return list.stream().map(assignmentMapper::toDto).collect(Collectors.toList());
  }

  // ================================================
  // Cập nhật Assignment
  // ================================================
  @Transactional
  public AssignmentResponseDto updateAssignment(Long id, AssignmentRequestDto dto) {
    Assignment assignment =
        assignmentRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Assignment not found"));

    // TODO: check instructor owns this course

    assignment.setTitle(dto.title());
    assignment.setDescription(dto.description());
    assignment.setDueAt(dto.dueAt());
    assignment.setMaxScore(dto.maxScore());
    // Nếu cho phép đổi lessonId thì fetch lesson mới và set lại

    return assignmentMapper.toDto(assignmentRepository.save(assignment));
  }

  // ================================================
  // Xoá Assignment (kèm submissions)
  // ================================================
  @Transactional
  public void deleteAssignment(Long id) {
    Assignment assignment =
        assignmentRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Assignment not found"));

    // TODO: check instructor owns this course

    // Xoá luôn submissions của assignment này
    submissionRepository.deleteAll(submissionRepository.findByAssignment_Id(id));
    assignmentRepository.delete(assignment);
  }

  // ================================================
  // Student nộp bài (cho phép nộp lại, sẽ ghi đè submission cũ)
  // ================================================
  @Transactional
  public AssignmentSubmissionResponseDto submitAssignment(AssignmentSubmissionRequestDto dto) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
      throw new AppException(401, "Unauthorized");
    }
    CustomUserDetails customUser = (CustomUserDetails) auth.getPrincipal();

    Long userId = customUser.getUserId();
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("Student not found"));

    Assignment assignment =
        assignmentRepository
            .findById(dto.assignmentId())
            .orElseThrow(() -> new NoSuchElementException("Assignment not found"));

    Lesson lesson = assignment.getLesson();
    if (lesson == null) {
      throw new AppException(500, "Assignment is not linked to any lesson");
    }

    Course course = lesson.getCourse();
    if (course == null) {
      throw new AppException(500, "Lesson is not linked to any course");
    }

    // ✅ Check student đã enroll course này chưa
    boolean enrolled = enrollService.isStudentEnrolledInCourse(student.getId(), course.getId());
    if (!enrolled) {
      throw new AppException(403, "You are not enrolled in this course");
    }

    LocalDateTime now = LocalDateTime.now();

    // ✅ Check deadline: không cho nộp sau hạn
    if (assignment.getDueAt() != null && now.isAfter(assignment.getDueAt())) {
      throw new AppException(400, "Assignment deadline is over");
    }

    // ✅ Cho phép nộp lại: nếu đã có submission thì ghi đè, reset điểm
    AssignmentSubmission submission =
        submissionRepository
            .findByAssignment_IdAndStudent_Id(assignment.getId(), student.getId())
            .orElseGet(
                () -> {
                  AssignmentSubmission s = new AssignmentSubmission();
                  s.setAssignment(assignment);
                  s.setStudent(student);
                  return s;
                });

    submission.setSubmittedAt(now);
    submission.setTextAnswer(dto.textAnswer());
    submission.setAttachmentUrl(dto.attachmentUrl());

    // Nếu nộp lại thì xoá điểm & nhận xét cũ (để giảng viên chấm lại)
    submission.setScore(null);
    submission.setFeedback(null);
    submission.setGradedAt(null);

    return submissionMapper.toDto(submissionRepository.save(submission));
  }

  // ================================================
  // Student: lấy bài nộp của chính mình
  // ================================================
  @Transactional(readOnly = true)
  public AssignmentSubmissionResponseDto getMySubmissionForAssignment(Long assignmentId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
      throw new AppException(401, "Unauthorized");
    }
    CustomUserDetails customUser = (CustomUserDetails) auth.getPrincipal();

    Long userId = customUser.getUserId();
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("Student not found"));

    return submissionRepository
        .findByAssignment_IdAndStudent_Id(assignmentId, student.getId())
        .map(submissionMapper::toDto)
        .orElse(null); // Controller trả 204 No Content nếu null
  }

  // ================================================
  // Instructor: danh sách submissions của 1 Assignment
  // ================================================
  @Transactional(readOnly = true)
  public List<AssignmentSubmissionResponseDto> getSubmissionsOfAssignment(Long assignmentId) {
    List<AssignmentSubmission> list = submissionRepository.findByAssignment_Id(assignmentId);
    return list.stream().map(submissionMapper::toDto).collect(Collectors.toList());
  }

  // ================================================
  // Instructor chấm điểm
  // ================================================
  @Transactional
  public AssignmentSubmissionResponseDto gradeSubmission(GradeSubmissionRequestDto dto) {
    AssignmentSubmission submission =
        submissionRepository
            .findById(dto.submissionId())
            .orElseThrow(() -> new NoSuchElementException("Submission not found"));

    // TODO: check instructor owns this course

    // ✅ Validate score không vượt quá maxScore (nếu được cấu hình)
    Assignment assignment = submission.getAssignment();
    Integer maxScore = assignment.getMaxScore();
    if (maxScore != null && dto.score() != null && dto.score() > maxScore) {
      throw new AppException(400, "Score cannot exceed maxScore " + maxScore);
    }

    submission.setScore(dto.score());
    submission.setFeedback(dto.feedback());
    submission.setGradedAt(LocalDateTime.now());

    return submissionMapper.toDto(submissionRepository.save(submission));
  }
}
