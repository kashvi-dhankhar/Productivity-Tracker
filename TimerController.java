package com.productivity.tracker.controller;

import com.productivity.tracker.dto.TaskSessionResponse;
import com.productivity.tracker.dto.TimerEndRequest;
import com.productivity.tracker.dto.TimerStartRequest;
import com.productivity.tracker.service.TimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timer")
@CrossOrigin(origins = "*")
public class TimerController {
    @Autowired
    private TimerService timerService;

    @PostMapping("/start")
    public ResponseEntity<TaskSessionResponse> startTimer(
            @RequestHeader("Authorization") String token,
            @RequestBody TimerStartRequest request) {
        try {
            Long userId = extractUserIdFromToken(token);
            TaskSessionResponse response = timerService.startTimer(userId, request.getTaskId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/end")
    public ResponseEntity<TaskSessionResponse> endTimer(
            @RequestHeader("Authorization") String token,
            @RequestBody TimerEndRequest request) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                System.err.println("Error: userId is null");
                return ResponseEntity.status(401).build();
            }
            if (request.getSessionId() == null) {
                System.err.println("Error: sessionId is null");
                return ResponseEntity.badRequest().build();
            }
            TaskSessionResponse response = timerService.endTimer(userId, request.getSessionId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error ending timer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Unexpected error ending timer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<TaskSessionResponse> getActiveSession(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            TaskSessionResponse response = timerService.getActiveSession(userId);
            if (response == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long extractUserIdFromToken(String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}

