package com.productivity.tracker.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductivitySummary {
    private LocalDate date;
    private Integer totalTasksCompleted;
    private Integer totalMinutesSpent;
    private BigDecimal averageEfficiency;
    private Integer easyTasksCompleted;
    private Integer moderateTasksCompleted;
    private Integer hardTasksCompleted;
    private Integer equivalentEasyTasks; // Based on conversion: 1 HARD = 4 EASY, 1 MODERATE = 2 EASY
    private List<TaskSessionSummary> taskSessions;

    @Data
    public static class TaskSessionSummary {
        private Long taskId;
        private String taskTitle;
        private String difficulty;
        private Integer estimatedMinutes;
        private Integer actualMinutes;
        private BigDecimal efficiencyPercentage;
    }
}

