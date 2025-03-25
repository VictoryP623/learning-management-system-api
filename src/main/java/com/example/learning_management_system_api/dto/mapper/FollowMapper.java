package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {

  @Mapping(target = "student.id", source = "studentId")
  @Mapping(target = "instructor.id", source = "instructorId")
  Follow toFollow(FollowRequestDto followRequestDto);

  @Mapping(target = "studentId", source = "student.id")
  @Mapping(target = "instructorId", source = "instructor.id")
  @Mapping(target = "createdAt", expression = "java(follow.getCreatedAt().toString())")
  @Mapping(target = "studentName", source = "student.user.fullname")
  @Mapping(target = "instructorName", source = "instructor.user.fullname")
  FollowResponseDto toResponseDto(Follow follow);

  UserResponseDto toUserResponseDto(User user);
}
