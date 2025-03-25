package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.CategoryRequestDto;
import com.example.learning_management_system_api.dto.response.CategoryResponseDto;
import com.example.learning_management_system_api.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  Category toEntity(CategoryRequestDto categoryRequestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateUserEntity(CategoryRequestDto sourceCategory, @MappingTarget Category targetCategory);

  CategoryResponseDto toResponseDTO(Category course);
}