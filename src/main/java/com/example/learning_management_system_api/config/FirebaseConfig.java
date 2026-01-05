package com.example.learning_management_system_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

  /**
   * Priority order: 1) FIREBASE_CREDENTIALS_PATH (Render Secret File:
   * /etc/secrets/firebase-key.json) 2) classpath resource: src/main/resources/firebase-key.json
   * (local dev)
   */
  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    GoogleCredentials credentials;

    String credentialsPath = System.getenv("FIREBASE_CREDENTIALS_PATH");
    if (credentialsPath != null && !credentialsPath.isBlank()) {
      try (InputStream is = new FileInputStream(credentialsPath)) {
        credentials = GoogleCredentials.fromStream(is);
      }
    } else {
      // Local fallback: src/main/resources/firebase-key.json
      try (InputStream is = new ClassPathResource("firebase-key.json").getInputStream()) {
        credentials = GoogleCredentials.fromStream(is);
      }
    }

    FirebaseOptions options =
        FirebaseOptions.builder()
            .setCredentials(credentials)
            .setStorageBucket("learning-management-syst-8d035.firebasestorage.app")
            .build();

    if (FirebaseApp.getApps().isEmpty()) {
      return FirebaseApp.initializeApp(options);
    }
    return FirebaseApp.getInstance();
  }
}
