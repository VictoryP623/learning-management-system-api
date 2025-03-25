package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

public record StudentResponseDto(
    String fullname, String email, LocalDateTime birthdate, String address) {}
