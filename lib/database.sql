-- ============================================================================
-- SwiftBus Management & Reservation System Database Schema (v3.0)
-- (C) 2026 SwiftBus Inc. All Rights Reserved.
-- ============================================================================
CREATE DATABASE IF NOT EXISTS bus_reservation;
USE bus_reservation;

-- Table: users (Authentication & Access Control)
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

-- Table: passenger (Traveler Registry with Disability Support)
CREATE TABLE IF NOT EXISTS passenger (
    passenger_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    is_disability TINYINT(1) NOT NULL DEFAULT 0,
    disability_type VARCHAR(100) NULL,
    disability_id VARCHAR(50) NULL,
    PRIMARY KEY (passenger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: passenger_disability (Dedicated Verified Concession Registry)
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

-- Table: bus
CREATE TABLE IF NOT EXISTS bus (
    bus_id INT NOT NULL,
    bus_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    fare DOUBLE NOT NULL DEFAULT 0.0,
    PRIMARY KEY (bus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: booking (Multi-seat Bookings & Disability Concession Support)
CREATE TABLE IF NOT EXISTS booking (
    booking_id INT NOT NULL AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    user_id INT NULL,
    bus_id INT NOT NULL,
    seat_number INT NOT NULL,
    booking_date VARCHAR(50) NOT NULL,
    fare DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    concession_amount DOUBLE NOT NULL DEFAULT 0.0,
    is_concession TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (booking_id),
    KEY fk_passenger (passenger_id),
    KEY fk_bus (bus_id),
    KEY idx_booking_user (user_id),
    KEY idx_booking_bus (bus_id),
    CONSTRAINT fk_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE,
    CONSTRAINT fk_bus FOREIGN KEY (bus_id) REFERENCES bus (bus_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed Initial Accounts (Hashed using PBKDF2WithHmacSHA256, Password for test1-test5 is 1234)
INSERT IGNORE INTO users (id, username, email, password_hash, salt, role) VALUES
(1, 'admin', 'admin@swiftbus.local', '89f41753c150531c3bfdb09a3cf6fa4360341ea226d7f02fcbc0768c67c5144b', 'a1b2c3d4e5f60718293a4b5c6d7e8f90', 'ADMIN'),
(2, 'john_doe', 'john@example.com', '3b1f5c66b8d234a667ec38a4c1647ea36014ba361309f44cf64b85da8158d601', 'f0e1d2c3b4a5968778695a4b3c2d1e0f', 'USER'),
(3, 'test1', 'test1@swiftbus.local', 'c49d61715c488c16c308ffa2aac8bf78265a6a418ad7b79dce96ffa62b4a8b28', '2acfec13c3b1548fc74ca21496de8f7e', 'USER'),
(4, 'test2', 'test2@swiftbus.local', '90ea0553f4c66a7f097db59e216a4a8ab7af0cc60441abcdf5630087828434b0', '2af96a45d3d19bbf76e9c7d52728f9e0', 'USER'),
(5, 'test3', 'test3@swiftbus.local', 'b13f1f69b9bc542ba6d9c68e8c0a455456d4790763c1f8629f3d2791c23e69e8', '06ae62b3adba6e9cac924438aa501325', 'USER'),
(6, 'test4', 'test4@swiftbus.local', '7a2de55354226f2c3c8a714bcdde8b25dbbf7bfd79f37be2f7653d4f340e0eed', 'a8abd5e482785b1a979e97c987f5430d', 'USER'),
(7, 'test5', 'test5@swiftbus.local', '2af439a4c4b5565de8a196f00f99fbc16759c43a195b0ddf6ad1cbae156d9658', '956dccb74876f321b9bdfaad4bc17701', 'USER');

-- Seed Passengers (Including Person with Disability for Concession Demonstration)
INSERT IGNORE INTO passenger (passenger_id, name, age, gender, phone, is_disability, disability_type, disability_id) VALUES
(101, 'John Doe', 28, 'Male', '9876543210', 0, NULL, NULL),
(102, 'Sarah Smith', 24, 'Female', '9123456780', 0, NULL, NULL),
(103, 'Michael Brown', 35, 'Male', '9988776655', 0, NULL, NULL),
(104, 'Robert Williams', 42, 'Male', '9811223344', 1, 'Locomotor / Mobility', 'UDID-98765-2026'),
(105, 'Elena Rostova', 29, 'Female', '9855667788', 1, 'Visual Impairment', 'UDID-55443-2026');

-- Seed Verified Disability Registry
INSERT IGNORE INTO passenger_disability (passenger_id, disability_type, certificate_no, concession_percentage, verified_status) VALUES
(104, 'Locomotor / Mobility', 'UDID-98765-2026', 50.0, 'VERIFIED'),
(105, 'Visual Impairment', 'UDID-55443-2026', 50.0, 'VERIFIED');

-- Seed Buses
INSERT IGNORE INTO bus (bus_id, bus_name, source, destination, total_seats, fare) VALUES
(1, 'Express Volvo AC', 'New York', 'Boston', 40, 45.00),
(2, 'City Deluxe Sleeper', 'New York', 'Washington DC', 36, 55.00),
(3, 'Metro Superfast', 'Boston', 'Philadelphia', 44, 40.00),
(4, 'Highway Star Express', 'Chicago', 'Detroit', 40, 50.00);

-- Seed Bookings (Demonstrating male/female seat occupations and 50% disability concession)
INSERT IGNORE INTO booking (booking_id, passenger_id, user_id, bus_id, seat_number, booking_date, fare, status, concession_amount, is_concession) VALUES
(1001, 101, 2, 1, 12, '2026-10-15', 45.00, 'CONFIRMED', 0.0, 0),
(1002, 102, 2, 1, 11, '2026-10-15', 55.00, 'CONFIRMED', 0.0, 0),
(1003, 104, 2, 1, 7, '2026-10-15', 22.50, 'CONFIRMED', 22.50, 1);
