-- Migration: Add avatar column to users table
-- Date: 2025-11-04
-- Description: Add avatar field to store user profile picture URL

ALTER TABLE users ADD COLUMN avatar TEXT NULL AFTER gender;

-- Optional: Add comment to the column
ALTER TABLE users MODIFY COLUMN avatar TEXT NULL COMMENT 'User profile avatar URL';
