package com.example.learning_management_system_api.utils.enums;

public enum LessonUnlockType {
  NONE, // không yêu cầu gì
  PREVIOUS_COMPLETED, // phải hoàn thành bài trước trong course
  SPECIFIC_LESSON_COMPLETED // phải hoàn thành bài có id = requiredLessonId
}
