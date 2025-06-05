package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UserRequestDto(
    @NotBlank(message = "Họ tên không được để trống")
        @Size(min = 3, max = 50, message = "Họ tên phải từ 3-50 ký tự")
        @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ\\s]+$",
            message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
        String fullname,
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ") LocalDateTime birthdate,
    @Size(max = 100, message = "Địa chỉ tối đa 100 ký tự")
        @Pattern(
            regexp = "^[a-zA-Z0-9À-ỹ\\s,.-/]+$",
            message = "Địa chỉ không được chứa ký tự đặc biệt")
        String address) {}
