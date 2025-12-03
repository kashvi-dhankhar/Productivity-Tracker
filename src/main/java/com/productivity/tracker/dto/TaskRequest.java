package com.productivity.tracker.dto;

import com.productivity.tracker.model.Task.Difficulty;
import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private Difficulty difficulty;
    private Integer estimatedMinutes;
}

