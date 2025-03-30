package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.ReportDTO;
import com.example.learning_management_system_api.dto.ReviewDTO;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Id.CartId;
import com.example.learning_management_system_api.entity.Id.EnrollId;
import com.example.learning_management_system_api.entity.Id.ReportId;
import com.example.learning_management_system_api.entity.Id.ReviewId;
import com.example.learning_management_system_api.entity.Student;

import java.util.List;
import java.util.Optional;

public interface IStudentService {

  Optional<Student> getStudentById(Long id);

  ReviewDTO submitReview(ReviewDTO review);

  ReviewDTO updateReview(ReviewDTO review);

  ReviewDTO getReview(ReviewId reviewId);

  String deleteReview(ReviewId reviewId);

  PageDto getEnrolledCourses(Long studentId, int page, int limit);

  PageDto getFollows(Long studentId, int page, int limit);

  String enrollCourse(EnrollId enrollId);

  PageDto searchInstructors(String name, int page, int limit);

  String addToCart(CartId cartId);

  PageDto getAllInCart(Long studentId, int page, int limit);

  String deleteItemInCart(CartId cartId);

  ReportDTO submitReport(ReportDTO report);

  ReportDTO updateReport(ReportDTO report);

  ReportDTO getReport(ReportId reportId);

  String deleteReport(ReportId reportId);

  List<ReviewDTO> getAllReviewsByCourseId(Long courseId);

  List<ReportDTO> getAllReportsByCourseId(Long courseId);
}