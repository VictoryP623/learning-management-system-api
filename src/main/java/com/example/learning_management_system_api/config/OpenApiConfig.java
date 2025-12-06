package com.example.learning_management_system_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI lmsOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("LMS API – Graduation Project")
                .description("Learning Management System – Java Spring Boot")
                .version("v1"));
  }
}
