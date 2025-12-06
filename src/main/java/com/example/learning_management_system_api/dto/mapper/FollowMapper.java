package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.User; // Đảm bảo class User không có lỗi biên dịch
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy; // Import cần thiết cho UnmappedTargetPolicy

// Đặt chính sách bỏ qua các trường không được ánh xạ rõ ràng để tránh lỗi biên dịch nếu DTO thiếu trường.
// Đây là giải pháp phòng ngừa, nhưng lỗi chính có thể nằm ở các file DTO/Entity.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FollowMapper {

  // Ánh xạ từ FollowRequestDto sang Entity Follow
  // Thiết lập ID của Student và Instructor lồng nhau
  @Mapping(target = "student.id", source = "studentId")
  @Mapping(target = "instructor.id", source = "instructorId")
  Follow toFollow(FollowRequestDto followRequestDto);

  // Ánh xạ từ Entity Follow sang FollowResponseDto
  @Mapping(target = "studentId", source = "student.id")
  @Mapping(target = "instructorId", source = "instructor.id")
  // Chuyển đổi đối tượng thời gian sang chuỗi
  @Mapping(target = "createdAt", expression = "java(follow.getCreatedAt().toString())")
  
  // SỬA LỖI ĐÃ KHẮC PHỤC: Sử dụng ánh xạ chuỗi thuộc tính an toàn (null-safe)
  // MapStruct sẽ tự động tạo mã kiểm tra null cho student -> user -> fullname
  @Mapping(target = "studentName", source = "student.user.fullname")
  @Mapping(target = "instructorName", source = "instructor.user.fullname")
  FollowResponseDto toResponseDto(Follow follow);

  // Ánh xạ từ Entity User sang UserResponseDto
  UserResponseDto toUserResponseDto(User user);
}
