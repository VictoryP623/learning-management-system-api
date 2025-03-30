package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.CategoryRequestDto;
import com.example.learning_management_system_api.dto.response.CategoryResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  @Autowired private ICategoryService categoryService;

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<?> updateCategoryName(
      @PathVariable Long id, @RequestBody CategoryRequestDto categoryRequestDto) {
    categoryService.updateCategoryName(id, categoryRequestDto);
    return ResponseVO.success("Category name updated successfully");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<?> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseVO.success("Category deleted successfully.");
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseVO<PageDto> getAllCategories(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String categoryName) {
    PageDto result = categoryService.getAllCategories(page, limit, categoryName);
    return ResponseVO.success(result);
  }

  @PostMapping("")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<CategoryResponseDto> addCategory(
      @RequestBody CategoryRequestDto categoryRequest) {

    CategoryResponseDto categoryResponse = categoryService.addCategory(categoryRequest);
    return ResponseVO.success(201, categoryResponse);
  }
}
