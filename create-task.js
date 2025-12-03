$(document).ready(function() {
    if (!checkAuth()) return;

    $('#taskForm').on('submit', function(e) {
        e.preventDefault();
        createTaskFromForm();
    });
});

function createTaskFromForm() {
    const title = document.getElementById('taskTitle').value;
    const description = document.getElementById('taskDescription').value;
    const difficulty = document.getElementById('taskDifficulty').value;
    const estimatedMinutes = parseInt(document.getElementById('estimatedMinutes').value);

    if (!title || !difficulty || !estimatedMinutes) {
        showAlert('Please fill in all required fields', 'danger');
        return;
    }

    const taskData = {
        title,
        description,
        difficulty,
        estimatedMinutes
    };

    createTask(taskData)
        .done(function(task) {
            showAlert('Task created successfully!', 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1500);
        })
        .fail(function(xhr) {
            if (xhr.status === 401) {
                logout();
            } else {
                showAlert(xhr.responseJSON?.message || 'Failed to create task. Please try again.', 'danger');
            }
        });
}

function showAlert(message, type = 'danger') {
    const alertDiv = document.getElementById('alertMessage');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.display = 'block';
    setTimeout(() => {
        alertDiv.style.display = 'none';
    }, 5000);
}

