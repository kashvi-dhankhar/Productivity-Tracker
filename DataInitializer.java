package com.productivity.tracker.config;

import com.productivity.tracker.model.User;
import com.productivity.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create sample users if they don't exist
        createUserIfNotExists("john_doe", "john.doe@example.com", "password123", "John Doe");
        createUserIfNotExists("jane_smith", "jane.smith@example.com", "password123", "Jane Smith");
        createUserIfNotExists("bob_wilson", "bob.wilson@example.com", "password123", "Bob Wilson");
        createUserIfNotExists("alice_brown", "alice.brown@example.com", "password123", "Alice Brown");
        createUserIfNotExists("charlie_davis", "charlie.davis@example.com", "password123", "Charlie Davis");
    }

    private void createUserIfNotExists(String username, String email, String password, String displayName) {
        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Created user: " + username + " (" + email + ")");
        } else {
            System.out.println("User already exists: " + username);
        }
    }
}

