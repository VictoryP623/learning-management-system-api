package com.example.learning_management_system_api;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningManagementSystemApiApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+07:00"));
    SpringApplication.run(LearningManagementSystemApiApplication.class, args);
  }
}
