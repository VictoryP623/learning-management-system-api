package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.RubricCriterionDto;
import com.example.learning_management_system_api.dto.response.RubricResponseDto;
import com.example.learning_management_system_api.entity.Rubric;
import com.example.learning_management_system_api.entity.RubricCriterion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RubricMapper {

  @Mapping(source = "assignment.id", target = "assignmentId")
  RubricResponseDto toDto(Rubric rubric);

  RubricCriterionDto toCriterionDto(RubricCriterion criterion);

  List<RubricCriterionDto> toCriterionDtoList(List<RubricCriterion> criteria);
}
