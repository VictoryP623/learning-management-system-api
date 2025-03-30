package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.SavedCourse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISaveCourseService {


    void saveCourse(SavedCourse savedCourse);
    boolean isCourseSaved(Long studentId, Long courseId);

    void deleteCourse(SavedCourse savedCourse);

    PageDto getSavedCoursesByStudentId(Long studentId, Pageable pageable);
}
