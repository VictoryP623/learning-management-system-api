package com.example.learning_management_system_api.dto.response;

import lombok.Data;

@Data
public class GoogleUserInfo {
    private String sub;       
    private String email;
    private String name;
    private String picture;
}
