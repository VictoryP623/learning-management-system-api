package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FollowMapper {

  // Entity -> Response
  @Mapping(target = "studentId", source = "student.id")
  @Mapping(target = "instructorId", source = "instructor.id")
  @Mapping(target = "studentName", source = "student.user.fullname")
  @Mapping(target = "instructorName", source = "instructor.user.fullname")
  @Mapping(
      target = "createdAt",
      expression = "java(follow.getCreatedAt() != null ? follow.getCreatedAt().toString() : null)")
  FollowResponseDto toResponseDto(Follow follow);

  // Dùng cho FollowService (getFollowedInstructors / getFollowedStudents)
  UserResponseDto toUserResponseDto(User user);
}
