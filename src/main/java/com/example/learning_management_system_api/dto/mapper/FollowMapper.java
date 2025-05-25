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
  @Mapping(target = "studentName", expression = "java(follow.getStudent() != null && follow.getStudent().getUser() != null ? follow.getStudent().getUser().getFullname() : null)")
  @Mapping(target = "instructorName", expression = "java(follow.getInstructor() != null && follow.getInstructor().getUser() != null ? follow.getInstructor().getUser().getFullname() : null)")
  
  FollowResponseDto toResponseDto(Follow follow);

  UserResponseDto toUserResponseDto(User user);
}
