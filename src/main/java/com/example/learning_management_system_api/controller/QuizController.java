package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.QuizRequestDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.QuizResponseDto;
import com.example.learning_management_system_api.entity.Quiz;
import com.example.learning_management_system_api.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseEntity<QuizResponseDto> createQuiz(@RequestBody @Valid QuizRequestDto quiz) {
        return new ResponseEntity<>(quizService.createQuiz(quiz), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseEntity<QuizResponseDto> getQuizById(@PathVariable Long id) {
        return new ResponseEntity<>(quizService.getQuizById(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseEntity<PageDto> getAllQuizzes(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam Long lessonId) {
        return ResponseEntity.ok(quizService.getAllQuizzes(lessonId, page, size));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseEntity<QuizResponseDto> updateQuiz(@PathVariable Long id, @RequestBody QuizRequestDto updatedQuiz) {
        return new ResponseEntity<>(quizService.updateQuiz(id, updatedQuiz), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_Instructor')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
