package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.StudentResponseDto;
import com.example.learning_management_system_api.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface StudentMapper {

  @Mappings({
    @Mapping(source = "user.fullname", target = "fullname"),
    @Mapping(source = "user.email", target = "email"),
    @Mapping(source = "user.birthdate", target = "birthdate"),
    @Mapping(source = "user.address", target = "address"),
  })
  StudentResponseDto toResponseDto(Student student);
}
