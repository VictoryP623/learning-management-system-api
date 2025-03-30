package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.mapper.WithdrawMapper;
import com.example.learning_management_system_api.dto.request.CourseDTO;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.EarningDTO;
import com.example.learning_management_system_api.dto.response.WithdrawResponseDTO;
import com.example.learning_management_system_api.entity.*;
import com.example.learning_management_system_api.repository.CategoryRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.WithdrawRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InstructorService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final WithdrawRepository withdrawRepository;
    private final InstructorRepository instructorRepository;
    private final CategoryRepository categoryRepository;
    private final LessonService lessonService;
    private final WithdrawMapper withdrawMapper;

    public InstructorService(CourseRepository courseRepository, CourseMapper courseMapper, WithdrawRepository withdrawRepository, InstructorRepository instructorRepository, CategoryRepository categoryRepository, LessonService lessonService, WithdrawMapper withdrawMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.withdrawRepository = withdrawRepository;
        this.instructorRepository = instructorRepository;
        this.categoryRepository = categoryRepository;
        this.lessonService = lessonService;
        this.withdrawMapper = withdrawMapper;
    }
    public CourseResponseDto createCourse(CourseDTO courseDTO) {
        Course courseResult = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Long userId = customUserDetails.getUserId();
            Course course = new Course(courseDTO);
            course.setInstructor(instructorRepository.findByUserId(userId));
            course.setStatus("PENDING");
            course.setCategory(categoryRepository.findById(courseDTO.getCategoryId()).get());
            courseResult = courseRepository.save(course);
        }

        return courseMapper.toResponseDTO(courseResult);
    }

    // Need Instructor, Category service to function properly
    public CourseResponseDto updateCourse(CourseDTO courseDTO) {
        Course courseResult = null;
        Course courseCheck = courseRepository.findById(courseDTO.getId()).orElseThrow(()-> new NoSuchElementException("Not found course"));
        lessonService.checkPermission(courseCheck);
        courseRepository.findById(courseDTO.getId()).orElseThrow(()-> new NoSuchElementException("Not found course"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Long userId = customUserDetails.getUserId();
            Course course = new Course(courseDTO);
            course.setInstructor(instructorRepository.findByUserId(userId));
            course.setStatus("PENDING");
            course.setCreatedAt(courseCheck.getCreatedAt());
            course.setCategory(categoryRepository.findById(courseDTO.getCategoryId()).orElseThrow(()-> new NoSuchElementException("Not found category")));
            courseResult = courseRepository.save(course);
        }
        return courseMapper.toResponseDTO(courseResult);
    }

    public String deleteCourse(Long id){Course courseResult = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Course course = courseRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Not found course"));
            lessonService.checkPermission(course);
            courseRepository.deleteById(id);
            return "Course deleted successfully";
        }
        return "Not instructor";
    }

    public List<EarningDTO> getEarning(Long id, LocalDateTime fromLocal, LocalDateTime toLocal) {
        List<EarningDTO> earningDTOs = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails){
            System.out.println("from: " + fromLocal);
            System.out.println("to: " + toLocal);
            earningDTOs = courseRepository.getEarnings(id, fromLocal, toLocal);
        }
        return earningDTOs;
    }

    public WithdrawResponseDTO withdraw(Long instructorId, Double amount){
        WithdrawResponseDTO withdrawResponseDTO = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails){
            Withdraw withdraw = new Withdraw();
            withdraw.setInstructorId(instructorId);
            withdraw.setAmount(amount);
            Withdraw result = withdrawRepository.save(withdraw);
            withdrawResponseDTO = withdrawMapper.toResponseDTO(result);
        }
        return withdrawResponseDTO;
    }

}
