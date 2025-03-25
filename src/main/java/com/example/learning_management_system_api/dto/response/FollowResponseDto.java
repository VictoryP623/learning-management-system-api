package com.example.learning_management_system_api.dto.response;


public record FollowResponseDto(
    Long studentId,
    Long instructorId,
    String createdAt,
    String studentName,
    String instructorName) {}
