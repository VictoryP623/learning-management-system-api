package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.QuizAttemptMapper;
import com.example.learning_management_system_api.dto.request.QuizAnswerDto;
import com.example.learning_management_system_api.dto.response.QuizAttemptResponseDto;
import com.example.learning_management_system_api.entity.AnswerOption;
import com.example.learning_management_system_api.entity.Quiz;
import com.example.learning_management_system_api.entity.QuizAttempt;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.repository.QuizAttemptRepository;
import com.example.learning_management_system_api.repository.QuizRepository;
import com.example.learning_management_system_api.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final LessonService lessonService;
    private final QuizAttemptMapper quizAttemptMapper;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository, QuizRepository quizRepository, UserRepository userRepository, LessonService lessonService, QuizAttemptMapper quizAttemptMapper) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.lessonService = lessonService;
        this.quizAttemptMapper = quizAttemptMapper;
    }

    public List<QuizAttemptResponseDto> checkAnswer(List<QuizAnswerDto> quizAnswers) {
        List<QuizAttempt> quizAttemptList = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Long userId = customUserDetails.getUserId();
            User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
            LocalDateTime currentTime = LocalDateTime.now();
            for (QuizAnswerDto quizAnswer : quizAnswers) {
                Quiz quiz = quizRepository.findById(quizAnswer.quizId()).orElseThrow(() -> new NoSuchElementException("Quiz id " + quizAnswer.quizId() + " is not found"));
                lessonService.checkGetPermission(quiz.getLesson().getCourse());
                AnswerOption answer = quiz.getAnswerOptions().stream()
                        .filter(option -> option.getKeyValue().equals(quizAnswer.answerId()))
                        .findFirst().orElseThrow(()->new NoSuchElementException("Answer id "+quizAnswer.answerId()+ " is not existed in quiz Id "+quizAnswer.quizId()));
                QuizAttempt quizAttempt = new QuizAttempt();
                quizAttempt.setQuiz(quiz);
                quizAttempt.setAnswerId(quizAnswer.answerId());
                quizAttempt.setAttemptTimestamp(currentTime);
                quizAttempt.setUser(user);
                quizAttempt.setCorrect(answer.getIsCorrect());
                quizAttemptList.add(quizAttempt);
            }
        }
        return quizAttemptRepository.saveAll(quizAttemptList).stream().map(quizAttemptMapper::toDto).toList();
    }

    public  Map<Long, Map<LocalDateTime, List<QuizAttempt>>> getQuizAttempt(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            Long userId = customUserDetails.getUserId();
            List<QuizAttempt> quizAttemptList = quizAttemptRepository.findByUserId(userId);

            return quizAttemptList.stream()
                    .collect(Collectors.groupingBy(
                            attempt -> attempt.getUser().getId(), // Group by userId instead of User object
                            Collectors.groupingBy(QuizAttempt::getAttemptTimestamp)
                    ));
        }
        return null;
    }
}
