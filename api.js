const API_BASE_URL = 'http://localhost:8080/api';

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'index.html';
        return false;
    }
    return true;
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    window.location.href = 'index.html';
}

// Task APIs
function createTask(taskData) {
    return $.ajax({
        url: `${API_BASE_URL}/tasks`,
        method: 'POST',
        headers: getAuthHeaders(),
        data: JSON.stringify(taskData)
    });
}

function getAllTasks() {
    return $.ajax({
        url: `${API_BASE_URL}/tasks`,
        method: 'GET',
        headers: getAuthHeaders()
    });
}

function getTaskById(taskId) {
    return $.ajax({
        url: `${API_BASE_URL}/tasks/${taskId}`,
        method: 'GET',
        headers: getAuthHeaders()
    });
}

function deleteTaskById(taskId) {
    return $.ajax({
        url: `${API_BASE_URL}/tasks/${taskId}`,
        method: 'DELETE',
        headers: getAuthHeaders()
    });
}

// Timer APIs
function startTimer(taskId) {
    return $.ajax({
        url: `${API_BASE_URL}/timer/start`,
        method: 'POST',
        headers: getAuthHeaders(),
        data: JSON.stringify({ taskId })
    });
}

function endTimer(sessionId) {
    if (!sessionId) {
        console.error('endTimer called with null/undefined sessionId');
        return $.Deferred().reject({ status: 400, responseJSON: { message: 'Session ID is required' } });
    }
    console.log('Calling endTimer API with sessionId:', sessionId);
    return $.ajax({
        url: `${API_BASE_URL}/timer/end`,
        method: 'POST',
        headers: getAuthHeaders(),
        data: JSON.stringify({ sessionId: sessionId }),
        error: function(xhr, status, error) {
            console.error('AJAX error ending timer:', status, error, xhr);
        }
    });
}

function getActiveSession() {
    return $.ajax({
        url: `${API_BASE_URL}/timer/active`,
        method: 'GET',
        headers: getAuthHeaders()
    });
}

// Productivity APIs
function getDailySummary(date) {
    const url = date 
        ? `${API_BASE_URL}/productivity/summary?date=${date}`
        : `${API_BASE_URL}/productivity/summary`;
    return $.ajax({
        url: url,
        method: 'GET',
        headers: getAuthHeaders()
    });
}

