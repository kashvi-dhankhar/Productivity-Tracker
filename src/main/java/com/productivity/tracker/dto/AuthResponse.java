package com.productivity.tracker.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private Long userId;

    public AuthResponse(String token, String username, String email, Long userId) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.userId = userId;
    }
}

