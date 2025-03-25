package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.UserRequestDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateUserEntity(UserRequestDto sourceUser, @MappingTarget User targetUser);

  UserResponseDto userToUserResponseDto(User user);
}
