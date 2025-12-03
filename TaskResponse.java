package com.productivity.tracker.dto;

import com.productivity.tracker.model.Task.Difficulty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Integer estimatedMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean completed; // True if task has at least one completed session (ended timer)
}

