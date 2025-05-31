package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.UserMapper;
import com.example.learning_management_system_api.dto.request.UpdateUserStatusRequest;
import com.example.learning_management_system_api.dto.request.UserRequestDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.exception.BadRequestException;
import com.example.learning_management_system_api.exception.NotFoundException;
import com.example.learning_management_system_api.repository.UserRepository;
import com.example.learning_management_system_api.utils.enums.UserRole;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Override
  @SneakyThrows
  public UserResponseDto updateUser(Long id, UserRequestDto userRequest) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("User with id " + id + "is not found"));
    userMapper.updateUserEntity(userRequest, user);

    CustomUserDetails userAccount =
        (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (!Objects.equals(userAccount.getUserId(), user.getId())
        && userAccount.getAuthorities().stream()
            .noneMatch(auth -> auth.getAuthority().equals("ROLE_Admin"))) {
      throw new AppException(403, "You do not allow to do this action");
    }

    return userMapper.userToUserResponseDto(userRepository.save(user));
  }

  @Override
  public Optional<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public PageDto getUsersByRole(UserRole role, Pageable pageable) {

    Page<User> userPage = userRepository.findByRole(role, pageable);

    List<Object> data =
        userPage.getContent().stream()
            .map(userMapper::userToUserResponseDto)
            .map(user -> (Object) user) // Chuyển đổi thành Object để phù hợp với PageDto
            .toList();
    return new PageDto(
        userPage.getNumber(),
        userPage.getSize(),
        userPage.getTotalPages(),
        userPage.getTotalElements(),
        data);
  }

  @Override
  public PageDto getUsers(Pageable pageable) {
    Page<User> userPage = userRepository.findAll(pageable);
    List<Object> data =
        userPage.getContent().stream()
            .map(userMapper::userToUserResponseDto)
            .map(user -> (Object) user) // Chuyển đổi thành Object để phù hợp với PageDto
            .toList();
    return new PageDto(
        userPage.getNumber(),
        userPage.getSize(),
        userPage.getTotalPages(),
        userPage.getTotalElements(),
        data);
  }

  @Override
  public UserResponseDto updateUserStatus(UpdateUserStatusRequest request, Long id) {
    Optional<User> optionalUser = userRepository.findById(id);

    if (optionalUser.isPresent()) {
      User user = optionalUser.get();

      // Không cho phép thay đổi status nếu là Admin
      if (user.getRole() == UserRole.Admin) {
        throw new BadRequestException("Không thể thay đổi trạng thái của Admin!");
      }

      if (request.getStatus() != null) {
        user.setStatus(request.getStatus());
        userRepository.save(user);
      }
      return userMapper.userToUserResponseDto(user);

    } else {
      throw new NotFoundException("User not found");
    }
  }

  // Nên để trả về DTO thay vì Optional<User>
  public UserResponseDto getUserByEmailUser(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return userMapper.toUserResponseDto(user);
  }
}