package com.example.learning_management_system_api.notification;

import com.example.learning_management_system_api.events.AdminEvents.*;
import com.example.learning_management_system_api.events.InstructorEvents.*;
import com.example.learning_management_system_api.events.StudentEvents;
import com.example.learning_management_system_api.events.StudentEvents.*;

public interface DomainNotifier {

  // ===================== Student =====================
  void handle(CourseStatusChangedEvent e);

  void handle(LessonCreatedEvent e);

  void handle(LessonUpdatedEvent e);

  void handle(QuizPublishedEvent e);

  void handle(StudyReminderEvent e);

  void handle(CourseCompletedEvent e);

  void handle(RefundStatusChangedEvent e);

  void handle(StudentEvents.AssignmentCreatedEvent e);

  // NEW (Sprint 2)
  void handle(AssignmentGradedEvent e);

  // ===================== Instructor =====================
  void handle(ReviewChangedEvent e);

  void handle(CourseReviewOutcomeEvent e);

  void handle(CoursePublishedToggledEvent e);

  void handle(ContentRemovedEvent e);

  void handle(OrderCreatedEvent e);

  void handle(RevenueDailySummaryEvent e);

  // NEW (Sprint 2)
  void handle(StudentEnrolledCourseEvent e);

  void handle(StudentSubmittedAssignmentEvent e);

  // ===================== Admin =====================
  void handle(NewInstructorPendingEvent e);

  void handle(CourseSubmittedForReviewEvent e);

  void handle(ContentReportedEvent e);
}
