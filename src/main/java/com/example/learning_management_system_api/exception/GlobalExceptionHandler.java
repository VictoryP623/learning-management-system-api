package com.example.learning_management_system_api.exception;

import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.JwtException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.naming.NoPermissionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${app.environment:development}")
  private String environment;

  private boolean isDevelopment() {
    return "development".equalsIgnoreCase(environment);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseVO<?>> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, List<String>> fieldErrors =
        ex.getBindingResult().getAllErrors().stream()
            .collect(
                Collectors.groupingBy(
                    error -> ((FieldError) error).getField(),
                    Collectors.mapping(t -> t.getDefaultMessage(), Collectors.toList())));

    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid input data",
            fieldErrors,
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ResponseVO<?>> handleDisabledUserException(DisabledException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.FORBIDDEN.value(),
            "Your account is disabled. Please contact support to reactivate your account.",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ResponseVO<?>> handleBadCredentialsException(BadCredentialsException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.UNAUTHORIZED.value(),
            "Incorrect password.",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ResponseVO<?>> handleJwtException(JwtException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.UNAUTHORIZED.value(),
            "Access denied: " + ex.getMessage() + " (Invalid or expired token)",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseVO<?>> handleAccessDeniedException(AccessDeniedException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.FORBIDDEN.value(),
            "You do not have permission to access this resource",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ResponseVO<?>> handleUsernameNotFoundException(
      UsernameNotFoundException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.NOT_FOUND.value(),
            "Account not found with the provided information",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ResponseVO<?>> handleAppException(AppException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            ex.getStatusCode(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatusCode()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseVO<?>> handleGenericException(Exception ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An error occurred. Please try again later",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ResponseVO<?>> maxFileSizeException(Exception ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            "File exceeds maximum size (20MB)",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ResponseVO<?>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    String missingParam = ex.getParameterName();
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            "Required parameter '" + missingParam + "' is missing",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<ResponseVO<?>> duplicateException(Exception ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ResponseVO<?>> handleNoResourceFoundException(NoResourceFoundException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.NOT_FOUND.value(),
            "Requested page not found",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ResponseVO<?>> handleNoSuchElementException(NoSuchElementException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoPermissionException.class)
  public ResponseEntity<ResponseVO<?>> handleNoPermissionException(NoPermissionException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.FORBIDDEN.value(),
            "You are not allowed to perform this action",
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ResponseVO<?>> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String paramName = ex.getName();
    String expectedType = ex.getRequiredType().getSimpleName();
    String message =
        String.format("The parameter '%s' must be of type %s", paramName, expectedType);

    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            message,
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ResponseVO<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ResponseVO<?>> handleIllegalStateException(IllegalStateException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ResponseVO<?>> handleBadRequest(BadRequestException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ResponseVO<?>> handleConflict(ConflictException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<ResponseVO<?>> handleDatabaseException(DatabaseException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public String getStackTraceAsString(Exception ex) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    ex.printStackTrace(printWriter);
    return stringWriter.toString();
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleJsonParseError(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("timestamp", LocalDateTime.now());
    errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
    errorDetails.put("error", "Bad Request");

    if (cause instanceof InvalidFormatException invalidFormatException) {

      String fieldName =
          invalidFormatException.getPath().stream()
              .filter(Objects::nonNull)
              .map(ref -> ((JsonMappingException.Reference) ref).getFieldName())
              .findFirst()
              .orElse("unknown");

      Class<?> targetType = invalidFormatException.getTargetType();
      if (targetType.equals(java.time.LocalDate.class)) {
        errorDetails.put(
            "message",
            "Error field '"
                + fieldName
                + "' : Invalid date format. Date must be in the format 'yyyy-MM-dd'");
      } else if (targetType.isEnum()) {
        Object[] enumConstants = targetType.getEnumConstants();
        String validValues =
            Arrays.stream(enumConstants).map(Object::toString).collect(Collectors.joining(", "));
        errorDetails.put(
            "message",
            String.format(
                "Error field '%s': Accepted values for enum '%s' are: [%s]",
                fieldName, targetType.getSimpleName(), validValues));
      } else {
        errorDetails.put(
            "message",
            String.format(
                "Error field '%s': %s is required", fieldName, targetType.getSimpleName()));
      }
    } else {
      errorDetails.put("message", ex.getMessage());
    }

    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ResponseVO<?>> handleNotFoundException(NotFoundException ex) {
    ResponseVO<?> response =
        ResponseVO.error(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            isDevelopment() ? getStackTraceAsString(ex) : null);

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }
}