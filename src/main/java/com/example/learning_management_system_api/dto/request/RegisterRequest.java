package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.utils.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequest {

  @NotBlank(message = "Email là bắt buộc")
  private String email;

  @NotBlank(message = "Password là bắt buộc")
  @Length(min = 6, message = "Vui lòng nhập ít nhất 6 ký tự")
  private String password;

  @NotBlank(message = "Full name là bắt buộc")
  private String fullname;

  @Pattern(
      regexp = "^Student|Instructor$",
      message = "Role phải là một trong {Student, Instructor}")
  private String role;

  public User toUser() {
    User user = new User();
    user.setEmail(this.email);
    user.setFullname(this.fullname);
    user.setRole(UserRole.valueOf(this.role));
    return user;
  }
}
