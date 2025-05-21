package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FollowMapper {

  @Mapping(target = "student.id", source = "studentId")
  @Mapping(target = "instructor.id", source = "instructorId")
  Follow toFollow(FollowRequestDto followRequestDto);

  @Mapping(target = "studentId", source = "student.id")
  @Mapping(target = "instructorId", source = "instructor.id")
  @Mapping(target = "createdAt", expression = "java(follow.getCreatedAt().toString())")
  // @Mapping(target = "studentName", source = "student.user.fullname")
  // @Mapping(target = "instructorName", source = "instructor.user.fullname")
  FollowResponseDto toResponseDto(Follow follow);

  @AfterMapping
  default void fillNames(
      Follow follow, @MappingTarget FollowResponseDto.FollowResponseDtoBuilder builder) {
    if (follow.getStudent() != null && follow.getStudent().getUser() != null) {
      builder.studentName(follow.getStudent().getUser().getFullname());
    }
    if (follow.getInstructor() != null && follow.getInstructor().getUser() != null) {
      builder.instructorName(follow.getInstructor().getUser().getFullname());
    }
  }

  UserResponseDto toUserResponseDto(User user);
}
