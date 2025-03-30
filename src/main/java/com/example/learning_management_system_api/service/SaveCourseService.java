package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.SavedCourseMapper;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.SavedCourseDTO;
import com.example.learning_management_system_api.entity.Id.SavedCourseId;
import com.example.learning_management_system_api.entity.SavedCourse;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.SavedCourseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SaveCourseService implements ISaveCourseService {

  @Autowired private SavedCourseRepository savedCourseRepository;
  @Autowired private SavedCourseMapper savedCourseMapper;
  @Autowired private CourseRepository courseRepository;
  @Override
  public void saveCourse(SavedCourse savedCourse) {

    courseRepository.findById(savedCourse.getCourseId()).orElseThrow(()->new NoSuchElementException("Not found course"));
    // Kiểm tra xem khóa học đã được lưu chưa
    if (isCourseSaved(savedCourse.getStudentId(), savedCourse.getCourseId())) {
      throw new AppException(400, "Course is already saved for this student.");
    }

    // Lưu khóa học
    savedCourseRepository.save(savedCourse);
  }

  @Override
  public boolean isCourseSaved(Long studentId, Long courseId) {
    SavedCourseId savedCourseId = new SavedCourseId();
    savedCourseId.setStudentId(studentId);
    savedCourseId.setCourseId(courseId);
    return savedCourseRepository.existsById(savedCourseId);
  }

  @Override
  public void deleteCourse(SavedCourse savedCourse) {
    SavedCourseId savedCourseId = new SavedCourseId();
    savedCourseId.setStudentId(savedCourse.getStudentId());
    savedCourseId.setCourseId(savedCourse.getCourseId());
    savedCourseRepository.findById(savedCourseId).orElseThrow(()->new NoSuchElementException("Not found course"));
    savedCourseRepository.delete(savedCourse);
  }

  public PageDto getSavedCoursesByStudentId(Long studentId, Pageable pageable) {
    Page<SavedCourse> savedCoursePage = savedCourseRepository.findByStudentId(studentId, pageable);

    List<SavedCourseDTO> savedCourseDTOs =
        savedCoursePage.getContent().stream()
            .map(savedCourseMapper::savedCourseToSavedCourseDTO)
            .collect(Collectors.toList());

    return new PageDto(
        savedCoursePage.getNumber(),
        savedCoursePage.getSize(),
        savedCoursePage.getTotalPages(),
        savedCoursePage.getTotalElements(),
        (List<Object>)
            (List<?>) savedCourseDTOs // Convert to List<Object> as per your existing code structure
        );
  }
}
