package com.productivity.tracker.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this main method to generate hashes for SQL scripts
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Verify
        System.out.println("Verification: " + encoder.matches(password, hash));
    }
}

