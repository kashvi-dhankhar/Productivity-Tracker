package com.productivity.tracker.service;

import com.productivity.tracker.dto.TaskRequest;
import com.productivity.tracker.dto.TaskResponse;
import com.productivity.tracker.model.Task;
import com.productivity.tracker.model.TaskSession;
import com.productivity.tracker.repository.TaskRepository;
import com.productivity.tracker.repository.TaskSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSessionRepository taskSessionRepository;

    public TaskResponse createTask(Long userId, TaskRequest request) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDifficulty(request.getDifficulty());
        task.setEstimatedMinutes(request.getEstimatedMinutes());

        task = taskRepository.save(task);
        return convertToResponse(task);
    }

    public List<TaskResponse> getAllTasks(Long userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return convertToResponse(task);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        // Check if there's an active timer for this task
        Optional<TaskSession> activeSession = taskSessionRepository.findByTaskIdAndEndTimeIsNull(taskId);
        if (activeSession.isPresent()) {
            throw new RuntimeException("Cannot delete task with an active timer. Please end the timer first.");
        }
        
        // The database cascade will handle deleting related task_sessions (ON DELETE CASCADE)
        taskRepository.delete(task);
    }

    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setDifficulty(task.getDifficulty());
        response.setEstimatedMinutes(task.getEstimatedMinutes());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        
        // Check if task has completed sessions (tasks with ended timers are considered completed)
        List<TaskSession> completedSessions = taskSessionRepository.findByTaskId(task.getId())
                .stream()
                .filter(s -> s.getEndTime() != null)
                .collect(Collectors.toList());
        response.setCompleted(completedSessions.size() > 0);
        
        return response;
    }
}

