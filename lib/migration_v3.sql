-- ============================================================================
-- Migration v3: Disability Registry, Multi-Booking & Concession Support
-- Bus Management & Reservation System (SwiftBus)
-- Target: MySQL 8.0+ / Debian & Pentium Architecture
-- ============================================================================

USE bus_reservation;

-- 1. Create dedicated passenger_disability table
CREATE TABLE IF NOT EXISTS passenger_disability (
    disability_reg_id INT NOT NULL AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    disability_type VARCHAR(100) NOT NULL,
    certificate_no VARCHAR(50) NOT NULL,
    concession_percentage DOUBLE NOT NULL DEFAULT 50.0,
    verified_status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (disability_reg_id),
    KEY fk_disability_passenger (passenger_id),
    CONSTRAINT fk_disability_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Add disability fields to passenger table
SET @exist_pwd := (SELECT COUNT(*) FROM information_schema.COLUMNS 
                   WHERE TABLE_SCHEMA = 'bus_reservation' 
                     AND TABLE_NAME = 'passenger' 
                     AND COLUMN_NAME = 'is_disability');

SET @query_pwd := IF(@exist_pwd = 0, 
    'ALTER TABLE passenger ADD COLUMN is_disability TINYINT(1) NOT NULL DEFAULT 0, ADD COLUMN disability_type VARCHAR(100) NULL, ADD COLUMN disability_id VARCHAR(50) NULL', 
    'SELECT "Disability columns already exist in passenger"');
PREPARE stmt FROM @query_pwd;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Add concession fields to booking table
SET @exist_concession := (SELECT COUNT(*) FROM information_schema.COLUMNS 
                          WHERE TABLE_SCHEMA = 'bus_reservation' 
                            AND TABLE_NAME = 'booking' 
                            AND COLUMN_NAME = 'concession_amount');

SET @query_concession := IF(@exist_concession = 0, 
    'ALTER TABLE booking ADD COLUMN concession_amount DOUBLE NOT NULL DEFAULT 0.0, ADD COLUMN is_concession TINYINT(1) NOT NULL DEFAULT 0', 
    'SELECT "Concession columns already exist in booking"');
PREPARE stmt FROM @query_concession;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Seed sample PwD passenger with 50% concession verification
INSERT IGNORE INTO passenger (passenger_id, name, age, gender, phone, is_disability, disability_type, disability_id) VALUES
(104, 'Robert Williams', 42, 'Male', '9811223344', 1, 'Locomotor / Mobility', 'UDID-98765-2026');

INSERT IGNORE INTO passenger_disability (passenger_id, disability_type, certificate_no, concession_percentage, verified_status) VALUES
(104, 'Locomotor / Mobility', 'UDID-98765-2026', 50.0, 'VERIFIED');

-- 5. Seed test1 to test5 test accounts (password: 1234)
INSERT IGNORE INTO users (id, username, email, password_hash, salt, role) VALUES
(3, 'test1', 'test1@swiftbus.local', 'c49d61715c488c16c308ffa2aac8bf78265a6a418ad7b79dce96ffa62b4a8b28', '2acfec13c3b1548fc74ca21496de8f7e', 'USER'),
(4, 'test2', 'test2@swiftbus.local', '90ea0553f4c66a7f097db59e216a4a8ab7af0cc60441abcdf5630087828434b0', '2af96a45d3d19bbf76e9c7d52728f9e0', 'USER'),
(5, 'test3', 'test3@swiftbus.local', 'b13f1f69b9bc542ba6d9c68e8c0a455456d4790763c1f8629f3d2791c23e69e8', '06ae62b3adba6e9cac924438aa501325', 'USER'),
(6, 'test4', 'test4@swiftbus.local', '7a2de55354226f2c3c8a714bcdde8b25dbbf7bfd79f37be2f7653d4f340e0eed', 'a8abd5e482785b1a979e97c987f5430d', 'USER'),
(7, 'test5', 'test5@swiftbus.local', '2af439a4c4b5565de8a196f00f99fbc16759c43a195b0ddf6ad1cbae156d9658', '956dccb74876f321b9bdfaad4bc17701', 'USER');
