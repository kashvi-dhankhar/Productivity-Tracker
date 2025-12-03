$(document).ready(function() {
    if (!checkAuth()) return;

    // Set default date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('summaryDate').value = today;
    
    loadSummary();
});

function loadSummary() {
    const date = document.getElementById('summaryDate').value;
    
    if (!date) {
        alert('Please select a date');
        return;
    }

    getDailySummary(date)
        .done(function(summary) {
            if (!summary) {
                const content = document.getElementById('summaryContent');
                content.innerHTML = `
                    <div class="alert alert-warning">
                        No data found for this date. Complete some tasks to see your productivity summary!
                    </div>
                `;
                return;
            }
            displaySummary(summary);
        })
        .fail(function(xhr) {
            console.error('Error loading summary:', xhr);
            if (xhr.status === 401) {
                logout();
            } else {
                const content = document.getElementById('summaryContent');
                const dateStr = date || new Date().toISOString().split('T')[0];
                content.innerHTML = `
                    <div class="alert alert-warning">
                        Failed to load summary for ${dateStr}. Please try again or check if you have completed any tasks on this date.
                    </div>
                `;
            }
        });
}

function displaySummary(summary) {
    const content = document.getElementById('summaryContent');
    
    if (!summary || summary.totalTasksCompleted === 0) {
        content.innerHTML = `
            <div class="alert alert-info">
                No tasks completed on ${summary.date}. Complete some tasks to see your productivity summary!
            </div>
        `;
        return;
    }

    const efficiencyClass = getEfficiencyClass(summary.averageEfficiency);
    const efficiencyColor = getEfficiencyColor(summary.averageEfficiency);

    let html = `
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="card summary-card easy">
                    <div class="card-body">
                        <h5 class="card-title">Easy Tasks</h5>
                        <h2>${summary.easyTasksCompleted || 0}</h2>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card summary-card moderate">
                    <div class="card-body">
                        <h5 class="card-title">Moderate Tasks</h5>
                        <h2>${summary.moderateTasksCompleted || 0}</h2>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card summary-card hard">
                    <div class="card-body">
                        <h5 class="card-title">Hard Tasks</h5>
                        <h2>${summary.hardTasksCompleted || 0}</h2>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Equivalent Easy Tasks</h5>
                        <h2>${summary.equivalentEasyTasks || 0}</h2>
                        <small class="text-muted">1 HARD = 4 EASY, 1 MODERATE = 2 EASY</small>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mb-4">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Total Tasks Completed</h5>
                        <h2>${summary.totalTasksCompleted}</h2>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Total Time Spent</h5>
                        <h2>${formatMinutes(summary.totalMinutesSpent || 0)}</h2>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Average Efficiency</h5>
                        <h2 class="${efficiencyClass}">${summary.averageEfficiency != null && summary.averageEfficiency > 0 
                            ? (typeof summary.averageEfficiency === 'number' 
                                ? summary.averageEfficiency.toFixed(2) 
                                : parseFloat(summary.averageEfficiency).toFixed(2)) 
                            : '0.00'}%</h2>
                        <small class="text-muted">${getEfficiencyMessage(summary.averageEfficiency)}</small>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h5>Task Sessions Details</h5>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Task</th>
                                <th>Difficulty</th>
                                <th>Estimated (min)</th>
                                <th>Actual (min)</th>
                                <th>Efficiency</th>
                            </tr>
                        </thead>
                        <tbody>
    `;

    if (summary.taskSessions && summary.taskSessions.length > 0) {
        summary.taskSessions.forEach(session => {
            // Calculate efficiency if not provided, or use provided value
            let sessionEfficiency = 'N/A';
            if (session.efficiencyPercentage != null && session.efficiencyPercentage !== undefined) {
                sessionEfficiency = (typeof session.efficiencyPercentage === 'number' 
                    ? session.efficiencyPercentage 
                    : parseFloat(session.efficiencyPercentage)).toFixed(2);
            } else if (session.actualMinutes != null && session.estimatedMinutes != null && session.actualMinutes > 0) {
                // Calculate efficiency: (estimated / actual) * 100
                sessionEfficiency = ((session.estimatedMinutes / session.actualMinutes) * 100).toFixed(2);
            }
            const sessionEfficiencyClass = getEfficiencyClass(session.efficiencyPercentage || 
                (session.actualMinutes && session.estimatedMinutes ? (session.estimatedMinutes / session.actualMinutes) * 100 : 0));
            html += `
                <tr>
                    <td>${escapeHtml(session.taskTitle)}</td>
                    <td><span class="badge ${getDifficultyBadgeClass(session.difficulty)}">${session.difficulty}</span></td>
                    <td>${session.estimatedMinutes}</td>
                    <td>${session.actualMinutes || 0}</td>
                    <td><span class="${sessionEfficiencyClass}">${sessionEfficiency}%</span></td>
                </tr>
            `;
        });
    } else {
        html += `
            <tr>
                <td colspan="5" class="text-center">No task sessions found</td>
            </tr>
        `;
    }

    html += `
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `;

    content.innerHTML = html;
}

function formatMinutes(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
        return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
}

function getEfficiencyClass(efficiency) {
    if (!efficiency) return '';
    const eff = typeof efficiency === 'number' ? efficiency : parseFloat(efficiency);
    if (eff >= 100) return 'efficiency-high';
    if (eff >= 80) return 'efficiency-medium';
    return 'efficiency-low';
}

function getEfficiencyColor(efficiency) {
    if (!efficiency) return '#6c757d';
    const eff = typeof efficiency === 'number' ? efficiency : parseFloat(efficiency);
    if (eff >= 100) return '#28a745';
    if (eff >= 80) return '#ffc107';
    return '#dc3545';
}

function getEfficiencyMessage(efficiency) {
    if (!efficiency) return 'No data';
    const eff = typeof efficiency === 'number' ? efficiency : parseFloat(efficiency);
    if (eff >= 100) return 'Great job! You finished early! 🎉';
    if (eff >= 80) return 'Good work!';
    if (eff >= 60) return 'Keep improving!';
    return 'You can do better!';
}

function getDifficultyBadgeClass(difficulty) {
    const badges = {
        'EASY': 'bg-success',
        'MODERATE': 'bg-warning text-dark',
        'HARD': 'bg-danger'
    };
    return badges[difficulty] || 'bg-secondary';
}

function endDay() {
    const date = document.getElementById('summaryDate').value || new Date().toISOString().split('T')[0];
    
    if (!confirm('Are you sure you want to end the day? This will generate your productivity summary.')) {
        return;
    }

    getDailySummary(date)
        .done(function(summary) {
            displaySummary(summary);
            // Show efficiency in alert if available
            if (summary.averageEfficiency && summary.averageEfficiency > 0) {
                const eff = typeof summary.averageEfficiency === 'number' 
                    ? summary.averageEfficiency.toFixed(2) 
                    : parseFloat(summary.averageEfficiency).toFixed(2);
                alert(`Day ended!\n\nYour productivity summary:\n- Tasks completed: ${summary.totalTasksCompleted}\n- Total time: ${formatMinutes(summary.totalMinutesSpent || 0)}\n- Average efficiency: ${eff}%`);
            } else {
                alert('Day ended! Check your productivity summary below.');
            }
        })
        .fail(function(xhr) {
            if (xhr.status === 404 || xhr.status === 400) {
                alert('No completed tasks found for this date. Complete some tasks and end the timer to see your productivity summary.');
            } else {
                alert('Failed to generate summary. Please try again.');
            }
        });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

