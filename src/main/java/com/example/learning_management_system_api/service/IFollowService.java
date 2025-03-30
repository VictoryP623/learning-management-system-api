package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;

import java.util.List;

public interface IFollowService {
    FollowResponseDto addFollow(Long userId, Long instructorId);
    void deleteFollow(Long userId,Long instructorId);
    PageDto getFollowedInstructors(Long studentId, int page, int size);
    PageDto getFollowedStudents(Long instructorId, int page, int size);
}
