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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    List<QuizAttemptResponseDto> result = new ArrayList<>();

    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new NoSuchElementException("User not found"));
      LocalDateTime currentTime = LocalDateTime.now();

      for (QuizAnswerDto answer : quizAnswers) {
        Long quizId = answer.quizId();
        List<Integer> chosenAnswerIds = answer.answerIds();
        Quiz quiz =
            quizRepository
                .findById(quizId)
                .orElseThrow(
                    () -> new NoSuchElementException("Quiz id " + quizId + " is not found"));
        lessonService.checkGetPermission(quiz.getLesson().getCourse());

        Set<Integer> correctAnswerIds =
            quiz.getAnswerOptions().stream()
                .filter(AnswerOption::getIsCorrect)
                .map(AnswerOption::getKeyValue)
                .collect(Collectors.toSet());

        Set<Integer> chosenSet = new HashSet<>(chosenAnswerIds);

        boolean isCorrect = chosenSet.equals(correctAnswerIds);

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setQuiz(quiz);
        quizAttempt.setAnswerId(null); // Vì nhiều đáp án, có thể để null
        quizAttempt.setAttemptTimestamp(currentTime);
        quizAttempt.setUser(user);
        quizAttempt.setCorrect(isCorrect);

        quizAttemptRepository.save(quizAttempt);

        QuizAttemptResponseDto responseDto =
            new QuizAttemptResponseDto(quiz.getId(), userId, null, isCorrect, currentTime);
        result.add(responseDto);
      }
    }
    return result;
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
