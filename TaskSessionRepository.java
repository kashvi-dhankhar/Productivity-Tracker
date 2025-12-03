package com.productivity.tracker.repository;

import com.productivity.tracker.model.TaskSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSessionRepository extends JpaRepository<TaskSession, Long> {
    List<TaskSession> findByUserId(Long userId);
    List<TaskSession> findByUserIdAndSessionDate(Long userId, LocalDate date);
    Optional<TaskSession> findByUserIdAndEndTimeIsNull(Long userId);
    List<TaskSession> findByTaskId(Long taskId);
    Optional<TaskSession> findByTaskIdAndEndTimeIsNull(Long taskId);
}

