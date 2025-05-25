package com.example.learning_management_system_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    // Load the service account key from src/main/resources
    GoogleCredentials credentials =
        GoogleCredentials.fromStream(new ClassPathResource("firebase-key.json").getInputStream());

    FirebaseOptions options =
        FirebaseOptions.builder()
            .setCredentials(credentials)
            .setStorageBucket("learning-management-syst-8d035.firebasestorage.app")
            .build();

    if (FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.initializeApp(options);
    } else {
      return FirebaseApp.getInstance();
    }
  }
}
