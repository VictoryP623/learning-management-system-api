package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponseDto(
    Long id, Double totalAmount, LocalDateTime createdAt, List<CourseResponseDto> course) {}
