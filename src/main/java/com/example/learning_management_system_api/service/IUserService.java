package com.example.learning_management_system_api.service;

import java.util.Optional;

import com.example.learning_management_system_api.dto.request.UpdateUserStatusRequest;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.utils.enums.UserRole;
import org.springframework.data.domain.Pageable;
import com.example.learning_management_system_api.dto.request.UserRequestDto;
import org.springframework.stereotype.Service;

import com.example.learning_management_system_api.entity.User;

@Service
public interface IUserService {

    public Optional<User> getUserByEmail(String email);
    PageDto getUsersByRole(UserRole role, Pageable pageable);

    PageDto getUsers(Pageable pageable);

    UserResponseDto updateUserStatus(UpdateUserStatusRequest request,Long id);

    UserResponseDto updateUser(Long id, UserRequestDto userRequest);

}
