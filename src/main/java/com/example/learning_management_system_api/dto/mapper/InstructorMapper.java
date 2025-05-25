package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.InstructorResponseDTO;
import com.example.learning_management_system_api.entity.Instructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

  @Mappings({
    @Mapping(source = "id", target = "id"),
    @Mapping(source = "user.fullname", target = "fullname"),
    @Mapping(source = "user.email", target = "email"),
  })
  InstructorResponseDTO toResponseDto(Instructor instructor);
}
