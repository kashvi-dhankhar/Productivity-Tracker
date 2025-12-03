package com.productivity.tracker.controller;

import com.productivity.tracker.dto.ProductivitySummary;
import com.productivity.tracker.service.ProductivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/productivity")
@CrossOrigin(origins = "*")
public class ProductivityController {
    @Autowired
    private ProductivityService productivityService;

    @GetMapping("/summary")
    public ResponseEntity<ProductivitySummary> getDailySummary(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).build();
            }
            ProductivitySummary summary = productivityService.getDailySummary(userId, date);
            // Always return OK, even if no tasks completed (summary will have 0 tasks)
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
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

