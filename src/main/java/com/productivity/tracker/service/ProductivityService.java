package com.productivity.tracker.service;

import com.productivity.tracker.dto.ProductivitySummary;
import com.productivity.tracker.model.Task;
import com.productivity.tracker.model.TaskSession;
import com.productivity.tracker.repository.TaskRepository;
import com.productivity.tracker.repository.TaskSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductivityService {
    @Autowired
    private TaskSessionRepository taskSessionRepository;

    @Autowired
    private TaskRepository taskRepository;

    public ProductivitySummary getDailySummary(Long userId, LocalDate date) {
        final LocalDate queryDate = (date == null) ? LocalDate.now() : date;

        // Get all sessions for the user and filter by date (based on session_date or end_date)
        // A session belongs to a date if it was started on that date OR ended on that date
        List<TaskSession> allUserSessions = taskSessionRepository.findByUserId(userId);
        
        // Filter sessions that either started or ended on the specified date
        List<TaskSession> sessions = allUserSessions.stream()
                .filter(s -> {
                    LocalDate sessionStartDate = s.getStartTime().toLocalDate();
                    LocalDate sessionEndDate = s.getEndTime() != null ? s.getEndTime().toLocalDate() : null;
                    // Session belongs to this date if it started or ended on this date
                    return sessionStartDate.equals(queryDate) || (sessionEndDate != null && sessionEndDate.equals(queryDate));
                })
                .collect(Collectors.toList());
        
        // Filter only completed sessions (with end time)
        List<TaskSession> completedSessions = sessions.stream()
                .filter(s -> s.getEndTime() != null)
                .collect(Collectors.toList());

        ProductivitySummary summary = new ProductivitySummary();
        summary.setDate(queryDate);
        summary.setTotalTasksCompleted(completedSessions.size());

        int totalMinutes = 0;
        int easyCount = 0;
        int moderateCount = 0;
        int hardCount = 0;
        BigDecimal totalEfficiency = BigDecimal.ZERO;
        int efficiencyCount = 0;

        for (TaskSession session : completedSessions) {
            if (session.getActualMinutes() != null) {
                totalMinutes += session.getActualMinutes();
            }

            if (session.getEfficiencyPercentage() != null) {
                totalEfficiency = totalEfficiency.add(session.getEfficiencyPercentage());
                efficiencyCount++;
            }

            Task task = taskRepository.findById(session.getTaskId()).orElse(null);
            if (task != null) {
                switch (task.getDifficulty()) {
                    case EASY:
                        easyCount++;
                        break;
                    case MODERATE:
                        moderateCount++;
                        break;
                    case HARD:
                        hardCount++;
                        break;
                }
            }
        }

        summary.setTotalMinutesSpent(totalMinutes);
        
        // Calculate average efficiency
        if (efficiencyCount > 0) {
            BigDecimal avgEfficiency = totalEfficiency.divide(
                    new BigDecimal(efficiencyCount), 2, RoundingMode.HALF_UP);
            summary.setAverageEfficiency(avgEfficiency);
        } else {
            summary.setAverageEfficiency(BigDecimal.ZERO);
        }

        summary.setEasyTasksCompleted(easyCount);
        summary.setModerateTasksCompleted(moderateCount);
        summary.setHardTasksCompleted(hardCount);

        // Calculate equivalent easy tasks
        // 1 HARD = 4 EASY, 1 MODERATE = 2 EASY
        int equivalentEasy = easyCount + (moderateCount * 2) + (hardCount * 4);
        summary.setEquivalentEasyTasks(equivalentEasy);

        // Set task session details
        List<ProductivitySummary.TaskSessionSummary> sessionSummaries = completedSessions.stream()
                .map(session -> {
                    Task task = taskRepository.findById(session.getTaskId()).orElse(null);
                    ProductivitySummary.TaskSessionSummary sessionSummary = 
                            new ProductivitySummary.TaskSessionSummary();
                    sessionSummary.setTaskId(session.getTaskId());
                    sessionSummary.setTaskTitle(task != null ? task.getTitle() : "Unknown");
                    sessionSummary.setDifficulty(task != null ? task.getDifficulty().name() : "UNKNOWN");
                    sessionSummary.setEstimatedMinutes(task != null ? task.getEstimatedMinutes() : 0);
                    sessionSummary.setActualMinutes(session.getActualMinutes());
                    sessionSummary.setEfficiencyPercentage(session.getEfficiencyPercentage());
                    return sessionSummary;
                })
                .collect(Collectors.toList());

        summary.setTaskSessions(sessionSummaries);

        return summary;
    }
}

