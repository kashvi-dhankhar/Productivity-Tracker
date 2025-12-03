let activeTimerInterval = null;
let activeSession = null;

$(document).ready(function() {
    if (!checkAuth()) return;

    const username = localStorage.getItem('username');
    if (username) {
        document.getElementById('usernameDisplay').textContent = username;
    }

    loadTasks();
    checkActiveTimer();
    
    // Check for active timer every 5 seconds
    setInterval(checkActiveTimer, 5000);
});

function loadTasks() {
    getAllTasks()
        .done(function(tasks) {
            const tasksList = document.getElementById('tasksList');
            const noTasksMessage = document.getElementById('noTasksMessage');
            
            if (tasks.length === 0) {
                tasksList.innerHTML = '';
                noTasksMessage.style.display = 'block';
                return;
            }

            noTasksMessage.style.display = 'none';
            tasksList.innerHTML = '';

            tasks.forEach(task => {
                const difficultyBadge = getDifficultyBadge(task.difficulty);
                const isCompleted = task.completed === true;
                const completedBadge = isCompleted ? '<span class="badge bg-success ms-2">✓ Completed</span>' : '';
                const cardClass = isCompleted ? 'card task-card task-completed' : 'card task-card';
                const card = `
                    <div class="col-md-4 mb-3">
                        <div class="${cardClass}">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <div>
                                        <h5 class="card-title mb-0">${escapeHtml(task.title)}${completedBadge}</h5>
                                    </div>
                                    <button class="btn btn-sm btn-outline-danger" onclick="deleteTask(${task.id}, '${escapeHtml(task.title)}')" title="Delete Task">
                                        <span>🗑️</span>
                                    </button>
                                </div>
                                <p class="card-text">${escapeHtml(task.description || 'No description')}</p>
                                <div class="mb-2">
                                    ${difficultyBadge}
                                    <span class="badge bg-secondary">${task.estimatedMinutes} min</span>
                                </div>
                                <button class="btn btn-primary btn-sm" onclick="startTaskTimer(${task.id})" ${isCompleted ? 'disabled title="Task already completed"' : ''}>
                                    ${isCompleted ? 'Task Completed' : 'Start Timer'}
                                </button>
                            </div>
                        </div>
                    </div>
                `;
                tasksList.innerHTML += card;
            });
        })
        .fail(function(xhr) {
            if (xhr.status === 401) {
                logout();
            } else {
                alert('Failed to load tasks. Please try again.');
            }
        });
}

function getDifficultyBadge(difficulty) {
    const badges = {
        'EASY': '<span class="badge bg-success">EASY</span>',
        'MODERATE': '<span class="badge bg-warning text-dark">MODERATE</span>',
        'HARD': '<span class="badge bg-danger">HARD</span>'
    };
    return badges[difficulty] || '<span class="badge bg-secondary">' + difficulty + '</span>';
}

function startTaskTimer(taskId) {
    startTimer(taskId)
        .done(function(session) {
            activeSession = session;
            showActiveTimer(session);
            alert(`Timer started for task: ${session.taskTitle}`);
            loadTasks(); // Refresh tasks list
        })
        .fail(function(xhr) {
            alert(xhr.responseJSON?.message || 'Failed to start timer. Please try again.');
        });
}

function checkActiveTimer() {
    getActiveSession()
        .done(function(session) {
            if (session) {
                activeSession = session;
                console.log('Active session found:', session);
                showActiveTimer(session);
            } else {
                hideActiveTimer();
            }
        })
        .fail(function(xhr) {
            if (xhr.status === 204 || xhr.status === 404) {
                // No active session - this is normal
                hideActiveTimer();
            } else {
                console.error('Error checking active timer:', xhr);
                hideActiveTimer();
            }
        });
}

function showActiveTimer(session) {
    const section = document.getElementById('activeTimerSection');
    const taskTitle = document.getElementById('activeTaskTitle');
    
    section.style.display = 'block';
    taskTitle.textContent = session.taskTitle;
    
    // Start timer display update
    if (activeTimerInterval) {
        clearInterval(activeTimerInterval);
    }
    
    activeTimerInterval = setInterval(() => {
        updateTimerDisplay(session.startTime);
    }, 1000);
    
    updateTimerDisplay(session.startTime);
}

function hideActiveTimer() {
    const section = document.getElementById('activeTimerSection');
    section.style.display = 'none';
    if (activeTimerInterval) {
        clearInterval(activeTimerInterval);
        activeTimerInterval = null;
    }
    activeSession = null;
}

function updateTimerDisplay(startTime) {
    const start = new Date(startTime);
    const now = new Date();
    const diff = now - start;
    
    const hours = Math.floor(diff / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    
    const display = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    document.getElementById('activeTimerDisplay').textContent = display;
}

function endActiveTimer() {
    if (!activeSession) {
        alert('No active timer session found.');
        return;
    }

    if (!activeSession.id) {
        console.error('Active session ID is missing:', activeSession);
        alert('Error: Session ID is missing. Please refresh the page and try again.');
        return;
    }

    if (!confirm('Are you sure you want to end the timer?')) {
        return;
    }

    console.log('Ending timer for session ID:', activeSession.id);
    
    endTimer(activeSession.id)
        .done(function(session) {
            console.log('Timer ended successfully:', session);
            hideActiveTimer();
            // Calculate efficiency properly - if efficiencyPercentage exists, use it, otherwise calculate
            let efficiency = 'N/A';
            if (session.efficiencyPercentage != null && session.efficiencyPercentage !== undefined) {
                efficiency = parseFloat(session.efficiencyPercentage).toFixed(2);
            } else if (session.actualMinutes != null && session.estimatedMinutes != null && session.actualMinutes > 0) {
                // Calculate efficiency: (estimated / actual) * 100
                efficiency = ((session.estimatedMinutes / session.actualMinutes) * 100).toFixed(2);
            }
            const message = `Timer ended!\n` +
                `Actual time: ${session.actualMinutes || 0} minutes\n` +
                `Estimated time: ${session.estimatedMinutes} minutes\n` +
                `Efficiency: ${efficiency}%`;
            alert(message);
            loadTasks();
            // Optionally redirect to summary page to see the completed task
            // Uncomment the line below if you want automatic redirect
            // window.location.href = 'summary.html';
        })
        .fail(function(xhr) {
            console.error('Error ending timer:', xhr);
            console.error('Status:', xhr.status);
            console.error('Response:', xhr.responseJSON || xhr.responseText);
            const errorMsg = xhr.responseJSON?.message || xhr.responseText || 'Failed to end timer. Please try again.';
            alert('Error: ' + errorMsg);
        });
}

function deleteTask(taskId, taskTitle) {
    // Check if this task has an active timer
    if (activeSession && activeSession.taskId === taskId) {
        alert('Cannot delete task with an active timer. Please end the timer first.');
        return;
    }

    if (!confirm(`Are you sure you want to delete the task "${taskTitle}"?\n\nThis action cannot be undone and will remove all associated timer sessions.`)) {
        return;
    }

    deleteTaskById(taskId)
        .done(function() {
            alert('Task deleted successfully!');
            loadTasks(); // Refresh the tasks list
        })
        .fail(function(xhr) {
            if (xhr.status === 401) {
                logout();
            } else if (xhr.status === 400) {
                const errorMsg = xhr.responseJSON?.message || 'Cannot delete task. It may have an active timer or other constraints.';
                alert('Error: ' + errorMsg);
            } else {
                alert('Failed to delete task. Please try again.');
            }
        });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

