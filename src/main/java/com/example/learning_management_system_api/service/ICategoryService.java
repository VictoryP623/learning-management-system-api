package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.CategoryRequestDto;
import com.example.learning_management_system_api.dto.response.CategoryResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import org.apache.coyote.BadRequestException;

public interface ICategoryService {

    void updateCategoryName(Long id, CategoryRequestDto categoryRequestDto);

    void deleteCategory(Long id);

    PageDto getAllCategories(int page, int limit, String categoryName);
    CategoryResponseDto addCategory(CategoryRequestDto categoryRequest);

}
