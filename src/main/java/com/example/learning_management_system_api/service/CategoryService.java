package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.CategoryMapper;
import com.example.learning_management_system_api.dto.request.CategoryRequestDto;
import com.example.learning_management_system_api.dto.response.CategoryResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Category;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.exception.ConflictException;
import com.example.learning_management_system_api.exception.DatabaseException;
import com.example.learning_management_system_api.repository.CategoryRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CourseRepository courseRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.categoryMapper = categoryMapper;
    }

  @Override
  public PageDto getAllCategories(int page, int limit, String categoryName) {
    Pageable pageable = PageRequest.of(page, limit);

    Page<Category> categoryPage;

    if (categoryName != null && !categoryName.isEmpty()) {
      categoryPage = categoryRepository.findByNameStartingWithIgnoreCase(categoryName, pageable);
    } else {
      categoryPage = categoryRepository.findAll(pageable);
    }

    List<Object> data =
        categoryPage.getContent().stream()
            .map(categoryMapper::toResponseDTO)
            .map(category -> (Object) category)
            .toList();

    return new PageDto(
        categoryPage.getNumber(),
        categoryPage.getSize(),
        categoryPage.getTotalPages(),
        categoryPage.getTotalElements(),
        data);
  }

    @Override
    @SneakyThrows
    public CategoryResponseDto addCategory(CategoryRequestDto categoryRequest) {
        if(categoryRequest.name() == null || categoryRequest.name().trim().isEmpty()){
            throw new AppException(400,"Category name cannot be empty.");
        }
        if(categoryRepository.existsByName(categoryRequest.name())){
            throw new ConflictException("Category name already exists.");
        }
        try {
            Category category=new Category();
            category.setName(categoryRequest.name());
//            category.setCreatedAt(LocalDateTime.now());
//            category.setUpdatedAt(LocalDateTime.now());

            Category savedCategory= categoryRepository.save(category);
            return categoryMapper.toResponseDTO(savedCategory);
        }catch (Exception e){
            throw new DatabaseException("Failed to saved category. Please try again later.",e);
        }

    }

    public void updateCategoryName(Long id, CategoryRequestDto categoryRequestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));

        // Map the DTO to the entity, ignoring null values


        categoryMapper.updateUserEntity(categoryRequestDto,category);

        // Save the updated category entity
        categoryRepository.save(category);
    }
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + id));
        List<Course> courseList = courseRepository.findByCategoryId(id);
        if(!courseList.isEmpty())
            throw new AppException(409,"Can't not delete because it contain course");

        categoryRepository.delete(category);  // Delete the category
    }
}
