package com.productivity.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskSessionResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer actualMinutes;
    private Integer estimatedMinutes;
    private BigDecimal efficiencyPercentage;
    private Boolean isActive;
}

