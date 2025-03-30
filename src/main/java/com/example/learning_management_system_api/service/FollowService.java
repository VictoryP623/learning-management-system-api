package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.FollowMapper;
import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.request.UserRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.Instructor;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.repository.FollowRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class FollowService implements IFollowService {
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private FollowMapper followMapper;

    @Override
    public FollowResponseDto addFollow(Long userId, Long instructorId) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        boolean alreadyFollowed = followRepository.existsByStudentIdAndInstructorId(
                student.getId(), instructorId);
        if (alreadyFollowed) {
            throw new IllegalStateException("You have already followed this instructor");
        }

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

        Follow follow = new Follow();
        follow.setStudentId(student.getId());
        follow.setInstructorId(instructorId);
        follow.setStudent(student);
        follow.setInstructor(instructor);

        Follow savedFollow = followRepository.save(follow);
        return followMapper.toResponseDto(savedFollow);
    }

    @Override
    public void deleteFollow(Long userId, Long instructorId) {
        Optional<Student> student = studentRepository.findByUserId(userId);
        if(student.isEmpty())
        {
            throw new NoSuchElementException("Student not found");
        }
        Long studentId = student.get().getId();
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }
        boolean alreadyFollowed = followRepository.existsByStudentIdAndInstructorId(
                studentId, instructorId);
        if(!alreadyFollowed){
            throw new NoSuchElementException("Follow relationship not found");
        }
        Follow follow = new Follow();
        follow.setStudentId(studentId);
        follow.setInstructorId(instructorId);

        followRepository.delete(follow);
    }

    @Override
    public PageDto getFollowedInstructors(Long studentId, int page, int size) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findAllByStudentId(studentId, pageRequest);

        List<UserResponseDto> instructors = follows.stream()
                .map(follow -> followMapper.toUserResponseDto(follow.getInstructor().getUser()))
                .collect(Collectors.toList());

        return new PageDto(
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalPages(),
                follows.getTotalElements(),
                new ArrayList<>(instructors)
        );
    }

    @Override
    public PageDto getFollowedStudents(Long instructorId, int page, int size) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findAllByInstructorId(instructorId, pageRequest);

        List<UserResponseDto> students = follows.stream()
                .map(follow -> followMapper.toUserResponseDto(follow.getStudent().getUser()))
                .collect(Collectors.toList());

        return new PageDto(
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalPages(),
                follows.getTotalElements(),
                new ArrayList<>(students)
        );
    }


}
