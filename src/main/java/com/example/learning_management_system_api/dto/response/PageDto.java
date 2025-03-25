package com.example.learning_management_system_api.dto.response;

import java.util.List;

public record PageDto(
    long pageNumber, long pageSize, long totalPages, long totalElements, List<Object> data) {}
