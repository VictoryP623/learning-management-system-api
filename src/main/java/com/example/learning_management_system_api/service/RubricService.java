package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.RubricMapper;
import com.example.learning_management_system_api.dto.request.RubricRequestDto;
import com.example.learning_management_system_api.dto.response.RubricResponseDto;
import com.example.learning_management_system_api.entity.Assignment;
import com.example.learning_management_system_api.entity.Rubric;
import com.example.learning_management_system_api.entity.RubricCriterion;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.AssignmentRepository;
import com.example.learning_management_system_api.repository.RubricRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RubricService {

  private final RubricRepository rubricRepository;
  private final AssignmentRepository assignmentRepository;
  private final RubricMapper rubricMapper;

  @Transactional
  public RubricResponseDto createOrUpdateRubric(Long assignmentId, RubricRequestDto dto) {
    Assignment assignment =
        assignmentRepository
            .findById(assignmentId)
            .orElseThrow(() -> new NoSuchElementException("Assignment not found"));

    // TODO: check instructor owns this course (giống check trong AssignmentService)

    Rubric rubric =
        rubricRepository
            .findByAssignment_Id(assignmentId)
            .orElseGet(
                () -> {
                  Rubric newRubric = new Rubric();
                  newRubric.setAssignment(assignment);
                  return newRubric;
                });

    rubric.setTitle(dto.title());
    rubric.setDescription(dto.description());

    // Clear old criteria, rebuild from request
    List<RubricCriterion> newCriteria = new ArrayList<>();
    dto.criteria()
        .forEach(
            input -> {
              RubricCriterion c = new RubricCriterion();
              c.setRubric(rubric);
              c.setName(input.name());
              c.setDescription(input.description());
              c.setMaxScore(input.maxScore());
              c.setOrderIndex(input.orderIndex());
              newCriteria.add(c);
            });

    rubric.getCriteria().clear();
    rubric.getCriteria().addAll(newCriteria);

    Rubric saved = rubricRepository.save(rubric);
    return rubricMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  public RubricResponseDto getRubricByAssignmentId(Long assignmentId) {
    Rubric rubric =
        rubricRepository
            .findByAssignment_Id(assignmentId)
            .orElseThrow(() -> new AppException(404, "Rubric not found for this assignment"));

    return rubricMapper.toDto(rubric);
  }
}
