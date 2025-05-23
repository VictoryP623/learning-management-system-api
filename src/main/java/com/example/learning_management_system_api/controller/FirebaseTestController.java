package com.example.learning_management_system_api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.FirebaseApp;

@RestController
@RequestMapping("/api/test")
//@CrossOrigin(origins = "http://localhost:3000")
public class FirebaseTestController {

    private final FirebaseApp firebaseApp;

    public FirebaseTestController(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    @GetMapping("/firebase")
    public String testFirebase() {
        return "✅ FirebaseApp name: " + firebaseApp.getName();
    }
}
