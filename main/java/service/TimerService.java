package com.productivity.tracker.service;

import com.productivity.tracker.dto.TaskSessionResponse;
import com.productivity.tracker.model.Task;
import com.productivity.tracker.model.TaskSession;
import com.productivity.tracker.repository.TaskRepository;
import com.productivity.tracker.repository.TaskSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TimerService {
    @Autowired
    private TaskSessionRepository taskSessionRepository;

    @Autowired
    private TaskRepository taskRepository;

    public TaskSessionResponse startTimer(Long userId, Long taskId) {
        // Check if there's already an active session
        Optional<TaskSession> activeSession = taskSessionRepository.findByUserIdAndEndTimeIsNull(userId);
        if (activeSession.isPresent()) {
            throw new RuntimeException("You already have an active timer session. Please end it first.");
        }

        // Verify task exists and belongs to user
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        TaskSession session = new TaskSession();
        session.setTaskId(taskId);
        session.setUserId(userId);
        session.setStartTime(LocalDateTime.now());
        session.setSessionDate(java.time.LocalDate.now());

        session = taskSessionRepository.save(session);
        return convertToResponse(session, task, true);
    }

    public TaskSessionResponse endTimer(Long userId, Long sessionId) {
        TaskSession session = taskSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        if (session.getEndTime() != null) {
            throw new RuntimeException("Timer session already ended");
        }

        Task task = taskRepository.findById(session.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getEstimatedMinutes() == null || task.getEstimatedMinutes() <= 0) {
            throw new RuntimeException("Task estimated minutes is invalid");
        }

        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        
        // Update sessionDate to the end date if the session spans across days
        // This ensures the session appears in the summary for the day it was completed
        LocalDate endDate = endTime.toLocalDate();
        LocalDate startDate = session.getStartTime().toLocalDate();
        // If session ended on a different day than it started, use the end date
        if (!endDate.equals(startDate)) {
            session.setSessionDate(endDate);
        }

        // Calculate actual time in minutes (with decimal precision for sub-minute durations)
        Duration duration = Duration.between(session.getStartTime(), endTime);
        long totalSeconds = duration.getSeconds();
        
        if (totalSeconds < 0) {
            throw new RuntimeException("Invalid duration: end time is before start time");
        }
        
        // Convert to minutes with decimal precision: 30 seconds = 0.5 minutes
        double actualMinutesDecimal = totalSeconds / 60.0;
        // Ensure minimum of 0.1 minutes (6 seconds) to avoid division by zero
        if (actualMinutesDecimal < 0.1) {
            actualMinutesDecimal = 0.1;
        }
        
        // Store as integer minutes (minimum 1 minute if less than 1 minute was spent)
        int actualMinutes = (int) Math.max(1, Math.round(actualMinutesDecimal));
        session.setActualMinutes(actualMinutes);

        // Calculate efficiency percentage using decimal minutes for accuracy
        // Efficiency = (estimated time / actual time) * 100
        // If finished early (actual < estimated), efficiency > 100%
        // If finished late (actual > estimated), efficiency < 100%
        BigDecimal efficiency = calculateEfficiency(task.getEstimatedMinutes(), actualMinutesDecimal);
        session.setEfficiencyPercentage(efficiency);

        session = taskSessionRepository.save(session);
        return convertToResponse(session, task, false);
    }

    private BigDecimal calculateEfficiency(Integer estimatedMinutes, double actualMinutesDecimal) {
        if (actualMinutesDecimal <= 0) {
            return BigDecimal.ZERO;
        }

        // Efficiency = (estimated / actual) * 100
        // Early finish = high efficiency (>100%)
        // Use decimal minutes for accurate calculation even for sub-minute durations
        BigDecimal estimated = new BigDecimal(estimatedMinutes);
        BigDecimal actual = new BigDecimal(actualMinutesDecimal);
        BigDecimal efficiency = estimated.divide(actual, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        return efficiency;
    }

    public TaskSessionResponse getActiveSession(Long userId) {
        Optional<TaskSession> activeSession = taskSessionRepository.findByUserIdAndEndTimeIsNull(userId);
        
        if (activeSession.isEmpty()) {
            return null;
        }

        TaskSession session = activeSession.get();
        Task task = taskRepository.findById(session.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return convertToResponse(session, task, true);
    }

    private TaskSessionResponse convertToResponse(TaskSession session, Task task, boolean isActive) {
        TaskSessionResponse response = new TaskSessionResponse();
        response.setId(session.getId());
        response.setTaskId(session.getTaskId());
        response.setTaskTitle(task.getTitle());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setActualMinutes(session.getActualMinutes());
        response.setEstimatedMinutes(task.getEstimatedMinutes());
        response.setEfficiencyPercentage(session.getEfficiencyPercentage());
        response.setIsActive(isActive);
        return response;
    }
}

