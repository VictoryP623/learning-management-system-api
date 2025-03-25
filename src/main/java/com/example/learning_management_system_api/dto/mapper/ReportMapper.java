package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.ReportDTO;
import com.example.learning_management_system_api.entity.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {
  ReportDTO toDto(Report report);

  Report toEntity(ReportDTO report);
}
