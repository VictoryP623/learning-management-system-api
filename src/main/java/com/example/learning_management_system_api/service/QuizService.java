package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.QuizMapper;
import com.example.learning_management_system_api.dto.request.QuizRequestDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.QuizResponseDto;
import com.example.learning_management_system_api.entity.AnswerOption;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.entity.Quiz;
import com.example.learning_management_system_api.events.StudentEvents;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.QuizRepository;
import com.example.learning_management_system_api.utils.enums.QuizType;
import com.example.learning_management_system_api.utils.enums.UserRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

  private final QuizRepository quizRepository;
  private final LessonRepository lessonRepository;
  private final QuizMapper quizMapper;
  private final LessonService lessonService;
  private final ApplicationEventPublisher publisher;

  public QuizService(
      QuizRepository quizRepository,
      LessonRepository lessonRepository,
      QuizMapper quizMapper,
      LessonService lessonService,
      ApplicationEventPublisher publisher) {
    this.quizRepository = quizRepository;
    this.lessonRepository = lessonRepository;
    this.quizMapper = quizMapper;
    this.lessonService = lessonService;
    this.publisher = publisher;
  }

  @SneakyThrows
  public QuizResponseDto createQuiz(QuizRequestDto quiz) {
    Lesson lesson =
        lessonRepository
            .findById(quiz.lessonId())
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));
    lessonService.checkPermission(lesson.getCourse());
    Quiz quizEntity = quizMapper.toEntity(quiz);
    quizEntity.setLesson(lesson);
    checkTypeQuick(quizEntity);
    if (quizEntity.getQuizType() == QuizType.ONE_CHOICE) {
      long correctAnswersCount =
          quizEntity.getAnswerOptions().stream().filter(AnswerOption::getIsCorrect).count();
      if (correctAnswersCount > 1) {
        throw new BadRequestException("ONE_CHOICE quiz must not have more than one correct answer");
      }
    }

    AtomicInteger counter = new AtomicInteger(1);
    quizEntity
        .getAnswerOptions()
        .forEach(answerOption -> answerOption.setKeyValue(counter.getAndIncrement()));

    Quiz saved = quizRepository.save(quizEntity);

    // publish QuizPublished (coi create là publish/mở)
    Long courseId = saved.getLesson().getCourse().getId();
    publisher.publishEvent(new StudentEvents.QuizPublishedEvent(courseId, saved.getId()));

    return quizMapper.toDto(saved);
  }

  public QuizResponseDto getQuizById(Long id) {
    Quiz quiz =
        quizRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    lessonService.checkGetPermission(quiz.getLesson().getCourse());
    return quizMapper.toDto(hideAnswer(quiz));
  }

  public PageDto getAllQuizzes(Long lessonId, int page, int size) {
    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));
    lessonService.checkGetPermission(lesson.getCourse());

    Pageable pageable = PageRequest.of(page, size);
    Page<Quiz> quizPage = quizRepository.findByLesson_Id(lessonId, pageable);

    // Hide answer nếu Student request
    List<QuizResponseDto> result =
        hideAnswer(quizPage.getContent()).stream().map(quizMapper::toDto).toList();

    return new PageDto(
        quizPage.getNumber(),
        quizPage.getSize(),
        quizPage.getTotalPages(),
        quizPage.getTotalElements(),
        new ArrayList<>(result));
  }

  public QuizResponseDto updateQuiz(Long id, QuizRequestDto updatedQuiz) {
    Quiz quiz =
        quizRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    lessonService.checkPermission(quiz.getLesson().getCourse());
    quizMapper.updateQuiz(updatedQuiz, quiz);
    if (updatedQuiz.lessonId() != null) {
      Lesson lesson =
          lessonRepository
              .findById(updatedQuiz.lessonId())
              .orElseThrow(() -> new NoSuchElementException("Lesson not found"));
      lessonService.checkPermission(lesson.getCourse());
      quiz.setLesson(lesson);
    }
    checkTypeQuick(quiz);
    if (!updatedQuiz.answerOptions().isEmpty()) {
      AtomicInteger counter = new AtomicInteger(1);
      quiz.getAnswerOptions()
          .forEach(answerOption -> answerOption.setKeyValue(counter.getAndIncrement()));
    }
    return quizMapper.toDto(quizRepository.save(quiz));
  }

  public void deleteQuiz(Long id) {
    Quiz quiz =
        quizRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    lessonService.checkPermission(quiz.getLesson().getCourse());
    quizRepository.deleteById(id);
  }

  // Ẩn đáp án đúng nếu user là Student (list quiz)
  List<Quiz> hideAnswer(List<Quiz> originalList) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
      // Không đăng nhập hoặc không phải CustomUserDetails → không ẩn đáp án
      return originalList;
    }

    Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
    boolean isStudent =
        authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> Objects.equals(role, "ROLE_" + UserRole.Student));

    if (isStudent) {
      originalList.stream()
          .flatMap(quiz -> quiz.getAnswerOptions().stream())
          .forEach(answerOption -> answerOption.setIsCorrect(null));
    }

    return originalList;
  }

  // Ẩn đáp án đúng nếu user là Student (1 quiz)
  Quiz hideAnswer(Quiz quiz) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
      return quiz;
    }

    Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
    boolean isStudent =
        authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> Objects.equals(role, "ROLE_" + UserRole.Student));

    if (isStudent) {
      quiz.getAnswerOptions().forEach(answerOption -> answerOption.setIsCorrect(null));
    }

    return quiz;
  }

  @SneakyThrows
  void checkTypeQuick(Quiz quizEntity) {
    if (quizEntity.getQuizType() == QuizType.ONE_CHOICE) {
      long correctAnswersCount =
          quizEntity.getAnswerOptions().stream().filter(AnswerOption::getIsCorrect).count();
      if (correctAnswersCount > 1) {
        throw new BadRequestException("ONE_CHOICE quiz must not have more than one correct answer");
      }
    }
  }
}
