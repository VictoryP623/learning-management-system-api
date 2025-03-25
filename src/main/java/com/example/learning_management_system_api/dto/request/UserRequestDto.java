package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.Past;
import java.time.LocalDateTime;

public record UserRequestDto(String fullname, @Past LocalDateTime birthdate, String address) {}
