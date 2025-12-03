const API_BASE_URL = 'http://localhost:8080/api';

function showRegister() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
}

function showLogin() {
    document.getElementById('registerForm').style.display = 'none';
    document.getElementById('loginForm').style.display = 'block';
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

function register() {
    const username = document.getElementById('regUsername').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;

    if (!username || !email || !password) {
        showAlert('Please fill in all fields');
        return;
    }

    $.ajax({
        url: `${API_BASE_URL}/auth/register`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ username, email, password }),
        success: function(response) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('username', response.username);
            window.location.href = 'dashboard.html';
        },
        error: function(xhr) {
            showAlert(xhr.responseJSON?.message || 'Registration failed. Please try again.');
        }
    });
}

function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showAlert('Please fill in all fields');
        return;
    }

    // Determine if input is email or username
    const isEmail = username.includes('@');
    const requestData = isEmail 
        ? { email: username, password }
        : { username, password };

    $.ajax({
        url: `${API_BASE_URL}/auth/login`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function(response) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('username', response.username);
            window.location.href = 'dashboard.html';
        },
        error: function(xhr) {
            showAlert(xhr.responseJSON?.message || 'Login failed. Please check your credentials.');
        }
    });
}

function loginWithGoogle() {
    // Placeholder for Google OAuth
    // In production, implement Google OAuth 2.0 flow
    showAlert('Google Sign-In is not yet configured. Please use email/password login.', 'info');
}

// Check if already logged in
$(document).ready(function() {
    const token = localStorage.getItem('token');
    if (token) {
        window.location.href = 'dashboard.html';
    }
});

