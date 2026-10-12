-- ============================================================================
-- Migration v2: User Accounts, Authentication & Performance Indexes
-- Bus Management & Reservation System (SwiftBus)
-- Target: MySQL 8.0+ / Pentium Low-Resource Deployment
-- ============================================================================

USE bus_reservation;

-- 1. Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Alter booking table to associate bookings with registered user accounts
-- Check and add user_id column if not exists
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = 'bus_reservation' 
                 AND TABLE_NAME = 'booking' 
                 AND COLUMN_NAME = 'user_id');

SET @query := IF(@exist = 0, 
    'ALTER TABLE booking ADD COLUMN user_id INT NULL AFTER passenger_id', 
    'SELECT "Column user_id already exists"');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Add foreign key and indexes on booking table
-- Add index on user_id
SET @exist_idx_user := (SELECT COUNT(*) FROM information_schema.STATISTICS 
                        WHERE TABLE_SCHEMA = 'bus_reservation' 
                          AND TABLE_NAME = 'booking' 
                          AND INDEX_NAME = 'idx_booking_user');
SET @query_idx_user := IF(@exist_idx_user = 0, 
    'ALTER TABLE booking ADD INDEX idx_booking_user (user_id)', 
    'SELECT "Index idx_booking_user already exists"');
PREPARE stmt FROM @query_idx_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add index on bus_id
SET @exist_idx_bus := (SELECT COUNT(*) FROM information_schema.STATISTICS 
                       WHERE TABLE_SCHEMA = 'bus_reservation' 
                         AND TABLE_NAME = 'booking' 
                         AND INDEX_NAME = 'idx_booking_bus');
SET @query_idx_bus := IF(@exist_idx_bus = 0, 
    'ALTER TABLE booking ADD INDEX idx_booking_bus (bus_id)', 
    'SELECT "Index idx_booking_bus already exists"');
PREPARE stmt FROM @query_idx_bus;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add foreign key constraint for user_id -> users(id)
SET @exist_fk_user := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
                       WHERE CONSTRAINT_SCHEMA = 'bus_reservation' 
                         AND TABLE_NAME = 'booking' 
                         AND CONSTRAINT_NAME = 'fk_booking_user');
SET @query_fk_user := IF(@exist_fk_user = 0, 
    'ALTER TABLE booking ADD CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL', 
    'SELECT "Foreign key fk_booking_user already exists"');
PREPARE stmt FROM @query_fk_user;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Seed initial default users (PBKDF2WithHmacSHA256, 65000 iterations)
-- Default Admin: username 'admin', password '1234'
-- Salt: a1b2c3d4e5f60718293a4b5c6d7e8f90
-- Pre-hashed PBKDF2: 89f41753c150531c3bfdb09a3cf6fa4360341ea226d7f02fcbc0768c67c5144b
-- Default User: username 'john_doe', password 'password123'
-- Salt: f0e1d2c3b4a5968778695a4b3c2d1e0f
-- Pre-hashed PBKDF2: 3b1f5c66b8d234a667ec38a4c1647ea36014ba361309f44cf64b85da8158d601
INSERT IGNORE INTO users (id, username, email, password_hash, salt, role) VALUES
(1, 'admin', 'admin@swiftbus.local', '89f41753c150531c3bfdb09a3cf6fa4360341ea226d7f02fcbc0768c67c5144b', 'a1b2c3d4e5f60718293a4b5c6d7e8f90', 'ADMIN'),
(2, 'john_doe', 'john@example.com', '3b1f5c66b8d234a667ec38a4c1647ea36014ba361309f44cf64b85da8158d601', 'f0e1d2c3b4a5968778695a4b3c2d1e0f', 'USER');

-- Associate existing seed bookings with the default user
UPDATE booking SET user_id = 2 WHERE user_id IS NULL;
