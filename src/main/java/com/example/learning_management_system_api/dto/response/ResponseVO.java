package com.example.learning_management_system_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseVO<T> {
  private int statusCode;
  private String message;
  private T data;
  private String error;
  private String stackTrace;

  public static <T> ResponseVO<T> success(T data) {
    return new ResponseVO<>(200, "Success", data, null, null);
  }

  public static <T> ResponseVO<T> success(int status, T data) {
    return new ResponseVO<>(status, "Success", data, null, null);
  }

  public static <T> ResponseVO<T> success(String message, T data) {
    return new ResponseVO<>(200, message, data, null, null);
  }

  public static <T> ResponseVO<T> success(String message) {
    return new ResponseVO<>(200, message, null, null, null);
  }

  public static ResponseVO<?> error(int statusCode, String error, String stackTrace) {
    return new ResponseVO<>(statusCode, null, null, error, stackTrace);
  }

  public static <T> ResponseVO<T> error(int statusCode, String error, T data, String stackTrace) {
    return new ResponseVO<>(statusCode, null, data, error, stackTrace);
  }

  public static ResponseVO<?> error(int statusCode, String error) {
    return new ResponseVO<>(statusCode, null, null, error, null);
  }
}
