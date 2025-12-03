package com.productivity.tracker.service;

import com.productivity.tracker.dto.AuthResponse;
import com.productivity.tracker.dto.LoginRequest;
import com.productivity.tracker.dto.RegisterRequest;
import com.productivity.tracker.model.User;
import com.productivity.tracker.repository.UserRepository;
import com.productivity.tracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = Optional.empty();
        
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userOpt = userRepository.findByUsername(request.getUsername());
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            userOpt = userRepository.findByEmail(request.getEmail());
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username/email or password");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username/email or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getId());
    }

    public AuthResponse loginWithGoogle(String googleId, String email, String username) {
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Create new user if doesn't exist
            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setUsername(username != null ? username : email.split("@")[0]);
            user.setPassword(passwordEncoder.encode("GOOGLE_AUTH")); // Placeholder
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getId());
    }
}

