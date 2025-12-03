package com.productivity.tracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before DataInitializer
public class DatabaseFixer implements CommandLineRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        fixTasksTable();
    }

    private void fixTasksTable() {
        try {
            // Check if tasks table exists and has correct structure
            String checkTable = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tasks'";
            Integer tableExists = jdbcTemplate.queryForObject(checkTable, Integer.class);
            
            if (tableExists != null && tableExists > 0) {
                // Check if title column exists (basic check)
                String checkTitle = "SELECT COUNT(*) FROM information_schema.columns " +
                                   "WHERE table_name = 'tasks' AND column_name = 'title'";
                Integer titleExists = jdbcTemplate.queryForObject(checkTitle, Integer.class);
                
                if (titleExists == null || titleExists == 0) {
                    System.out.println("Tasks table structure is incorrect. Recreating table...");
                    // Drop foreign key constraints first
                    try {
                        jdbcTemplate.execute("ALTER TABLE task_sessions DROP CONSTRAINT IF EXISTS task_sessions_task_id_fkey");
                    } catch (Exception e) {
                        // Ignore
                    }
                    // Drop and recreate table
                    jdbcTemplate.execute("DROP TABLE IF EXISTS tasks CASCADE");
                    createTasksTable();
                    System.out.println("✓ Recreated tasks table");
                } else {
                    // Table exists, just add missing columns
                    addMissingColumns();
                }
            } else {
                // Table doesn't exist, create it
                createTasksTable();
                System.out.println("✓ Created tasks table");
            }
        } catch (Exception e) {
            System.err.println("Error fixing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTasksTable() {
        jdbcTemplate.execute("CREATE TABLE tasks (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('EASY', 'MODERATE', 'HARD')), " +
                "estimated_minutes INTEGER NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id)");
    }
    
    private void addMissingColumns() {
        try {
            // Check and add estimated_minutes
            String checkEstimated = "SELECT COUNT(*) FROM information_schema.columns " +
                                   "WHERE table_name = 'tasks' AND column_name = 'estimated_minutes'";
            Integer estimatedExists = jdbcTemplate.queryForObject(checkEstimated, Integer.class);
            if (estimatedExists == null || estimatedExists == 0) {
                jdbcTemplate.execute("ALTER TABLE tasks ADD COLUMN estimated_minutes INTEGER");
                jdbcTemplate.execute("UPDATE tasks SET estimated_minutes = 30 WHERE estimated_minutes IS NULL");
                jdbcTemplate.execute("ALTER TABLE tasks ALTER COLUMN estimated_minutes SET NOT NULL");
                jdbcTemplate.execute("ALTER TABLE tasks ALTER COLUMN estimated_minutes SET DEFAULT 30");
                System.out.println("✓ Added estimated_minutes column");
            }
            
            // Check and add difficulty
            String checkDifficulty = "SELECT COUNT(*) FROM information_schema.columns " +
                                   "WHERE table_name = 'tasks' AND column_name = 'difficulty'";
            Integer difficultyExists = jdbcTemplate.queryForObject(checkDifficulty, Integer.class);
            if (difficultyExists == null || difficultyExists == 0) {
                jdbcTemplate.execute("ALTER TABLE tasks ADD COLUMN difficulty VARCHAR(20)");
                jdbcTemplate.execute("UPDATE tasks SET difficulty = 'EASY' WHERE difficulty IS NULL");
                jdbcTemplate.execute("ALTER TABLE tasks ALTER COLUMN difficulty SET NOT NULL");
                jdbcTemplate.execute("ALTER TABLE tasks ALTER COLUMN difficulty SET DEFAULT 'EASY'");
                try {
                    jdbcTemplate.execute("ALTER TABLE tasks ADD CONSTRAINT chk_difficulty CHECK (difficulty IN ('EASY', 'MODERATE', 'HARD'))");
                } catch (Exception e) {
                    // Constraint might exist
                }
                System.out.println("✓ Added difficulty column");
            }
        } catch (Exception e) {
            System.err.println("Error adding missing columns: " + e.getMessage());
        }
    }
}

