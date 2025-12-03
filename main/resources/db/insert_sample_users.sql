-- Sample users with BCrypt hashed passwords
-- Password for all users: password123
-- BCrypt hash generated for "password123"

-- Insert sample users
-- Note: If using PostgreSQL, you may need to handle conflicts differently
INSERT INTO users (username, email, password, created_at, updated_at)
SELECT * FROM (VALUES 
    ('john_doe', 'john.doe@example.com', '$2a$10$Y6AC4fG2vizPZL7.N7e4ve.re2vdZVnx9NGMQaAsJxTvKE94.d1Uq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('jane_smith', 'jane.smith@example.com', '$2a$10$Y6AC4fG2vizPZL7.N7e4ve.re2vdZVnx9NGMQaAsJxTvKE94.d1Uq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob_wilson', 'bob.wilson@example.com', '$2a$10$Y6AC4fG2vizPZL7.N7e4ve.re2vdZVnx9NGMQaAsJxTvKE94.d1Uq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('alice_brown', 'alice.brown@example.com', '$2a$10$Y6AC4fG2vizPZL7.N7e4ve.re2vdZVnx9NGMQaAsJxTvKE94.d1Uq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('charlie_davis', 'charlie.davis@example.com', '$2a$10$Y6AC4fG2vizPZL7.N7e4ve.re2vdZVnx9NGMQaAsJxTvKE94.d1Uq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
) AS v(username, email, password, created_at, updated_at)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE users.username = v.username OR users.email = v.email);

-- Note: The BCrypt hash above is for "password123"
-- If users already exist, they will be skipped due to ON CONFLICT clause

