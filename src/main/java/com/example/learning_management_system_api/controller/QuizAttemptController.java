package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.QuizAnswerDto;
import com.example.learning_management_system_api.dto.response.QuizAttemptResponseDto;
import com.example.learning_management_system_api.entity.QuizAttempt;
import com.example.learning_management_system_api.service.QuizAttemptService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizAttempts")
//@CrossOrigin(origins = "http://localhost:3000")
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_Student')")
    public ResponseEntity<List<QuizAttemptResponseDto>> getAllQuizzes(@RequestBody @Valid List<QuizAnswerDto> quizAnswers) {
        return new ResponseEntity<>(quizAttemptService.checkAnswer(quizAnswers), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseEntity<Map<Long, Map<LocalDateTime, List<QuizAttempt>>>> getAllQuizzes() {
        return new ResponseEntity<>(quizAttemptService.getQuizAttempt(), HttpStatus.OK);
    }
}
