package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.LessonResourceDto;
import com.example.learning_management_system_api.entity.LessonResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LessonResourceMapper {

    @Mappings({
            @Mapping(source = "lesson.id", target = "lessonId"),
    })
    LessonResourceDto toDto(LessonResource lessonResource);
}
