package com.productivity.tracker.controller;

import com.productivity.tracker.dto.TaskRequest;
import com.productivity.tracker.dto.TaskResponse;
import com.productivity.tracker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestHeader("Authorization") String token,
            @RequestBody TaskRequest request) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }
            TaskResponse response = taskService.createTask(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }
            List<TaskResponse> tasks = taskService.getAllTasks(userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Long userId = extractUserIdFromToken(token);
            TaskResponse task = taskService.getTaskById(id, userId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }
            taskService.deleteTask(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    private Long extractUserIdFromToken(String token) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                if (authentication.getPrincipal() instanceof Long) {
                    return (Long) authentication.getPrincipal();
                }
                // If principal is a string (username), we need to get userId from token
                // This shouldn't happen with our current setup, but handle it gracefully
                System.out.println("Authentication principal type: " + authentication.getPrincipal().getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("Error extracting userId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

