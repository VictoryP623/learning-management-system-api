package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LessonResourceDto{
    private Long id;
    private String url;
    private String name;
    private Long lessonId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
