-- ============================================================
-- CAMPUS LOST AND FOUND MANAGEMENT SYSTEM
-- Database Schema and Sample Data
-- Database: MySQL 8.0+
-- Database Name: campus_lost_found
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_lost_found
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_lost_found;

-- Drop child tables first if existing
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE
-- user_id (PK, auto increment), name, email (unique), password, role (ENUM 'student','admin'), created_at (timestamp, default current)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'admin') NOT NULL DEFAULT 'student',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. ITEMS TABLE
-- item_id (PK), title, description, category, location, date_reported, status (ENUM 'lost','found','claimed'), reported_by (FK to users)
CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(60) NOT NULL,
    location VARCHAR(150) NOT NULL,
    date_reported DATE NOT NULL,
    status ENUM('lost', 'found', 'claimed') NOT NULL DEFAULT 'lost',
    reported_by INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_reported_by
        FOREIGN KEY (reported_by) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_items_title (title),
    INDEX idx_items_category (category),
    INDEX idx_items_location (location),
    INDEX idx_items_status (status)
) ENGINE=InnoDB;

-- 3. CLAIMS TABLE
-- claim_id (PK), item_id (FK to items), claimant_id (FK to users), message, status (ENUM 'pending','approved','rejected', default 'pending'), claim_date (timestamp)
CREATE TABLE claims (
    claim_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    claimant_id INT NOT NULL,
    message TEXT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    claim_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claims_item
        FOREIGN KEY (item_id) REFERENCES items(item_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant
        FOREIGN KEY (claimant_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_claims_item (item_id),
    INDEX idx_claims_claimant (claimant_id),
    INDEX idx_claims_status (status)
) ENGINE=InnoDB;

-- ============================================================
-- SAMPLE DATA
-- 1 Admin, 3 Students, 6 Items, 3 Claims
-- ============================================================

-- Users: 1 Admin and 3 Students (Passwords stored as plain or standard demo passwords, e.g. admin123, student123)
INSERT INTO users (user_id, name, email, password, role) VALUES
(1, 'System Administrator', 'admin@campus.edu', 'admin123', 'admin'),
(2, 'Alice Smith', 'alice@student.campus.edu', 'student123', 'student'),
(3, 'Bob Johnson', 'bob@student.campus.edu', 'student123', 'student'),
(4, 'Charlie Brown', 'charlie@student.campus.edu', 'student123', 'student');

-- Reset Auto Increment
ALTER TABLE users AUTO_INCREMENT = 5;

-- Items: 6 items (lost, found, claimed)
INSERT INTO items (item_id, title, description, category, location, date_reported, status, reported_by) VALUES
(1, 'Blue Hydro Flask Water Bottle', 'Dark blue 32oz bottle with stickers on side', 'Accessories', 'Central Library 1st Floor', '2026-10-20', 'found', 1),
(2, 'HP Spectre Laptop 14-inch', 'Silver laptop in a grey neoprene sleeve', 'Electronics', 'Science Block Computer Lab 2', '2026-10-22', 'lost', 2),
(3, 'Scientific Calculator TI-84 Plus', 'Black casing with initials BJ engraved on back', 'Electronics', 'Engineering Hall Room 104', '2026-10-24', 'found', 3),
(4, 'Black Leather Bi-fold Wallet', 'Contains student ID card and bus pass', 'Personal Belongings', 'Student Cafeteria', '2026-10-25', 'claimed', 4),
(5, 'Campus ID Card - Alice Smith', 'Plastic student ID card ending in 4491', 'Cards & Documents', 'Sports Complex Gymnasium', '2026-10-26', 'found', 1),
(6, 'Apple AirPods Pro 2nd Gen', 'White wireless earbuds in charging case with Pikachu keychain', 'Electronics', 'Auditorium Hall B', '2026-10-28', 'lost', 2);

-- Reset Auto Increment
ALTER TABLE items AUTO_INCREMENT = 7;

-- Claims: 3 claims
INSERT INTO claims (claim_id, item_id, claimant_id, message, status, claim_date) VALUES
(1, 1, 2, 'Hi, I lost my blue Hydro Flask in the library near desk 14. It has a NASA sticker.', 'pending', '2026-10-21 14:30:00'),
(2, 4, 4, 'This is my black wallet, I dropped it during lunch. Has my ID card inside.', 'approved', '2026-10-25 13:15:00'),
(3, 3, 2, 'I misplaced my TI-84 calculator after calculus class, can verify battery cover tape.', 'rejected', '2026-10-26 09:45:00');

-- Reset Auto Increment
ALTER TABLE claims AUTO_INCREMENT = 4;
