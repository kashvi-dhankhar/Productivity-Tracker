-- Fix tasks table if estimated_minutes column is missing
-- Run this if you get "column estimated_minutes does not exist" error

-- Check if column exists, if not add it
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tasks' 
        AND column_name = 'estimated_minutes'
    ) THEN
        ALTER TABLE tasks ADD COLUMN estimated_minutes INTEGER NOT NULL DEFAULT 30;
    END IF;
END $$;

-- Also ensure other columns exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tasks' 
        AND column_name = 'difficulty'
    ) THEN
        ALTER TABLE tasks ADD COLUMN difficulty VARCHAR(20) NOT NULL DEFAULT 'EASY';
        ALTER TABLE tasks ADD CONSTRAINT chk_difficulty CHECK (difficulty IN ('EASY', 'MODERATE', 'HARD'));
    END IF;
END $$;

