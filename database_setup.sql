-- ATM Database Setup Script
-- Run this in MySQL to create the required database and tables

-- Create database
CREATE DATABASE IF NOT EXISTS atm_db;

-- Use the database
USE atm_db;

-- Create users table (for balance and PIN)
CREATE TABLE IF NOT EXISTS users (
    card_number VARCHAR(20) PRIMARY KEY,
    pin INT NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00
);

-- Create transactions table (for transaction history)
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (card_number) REFERENCES users(card_number)
);

-- Insert test users (matching the code)
INSERT INTO users (card_number, pin, balance) VALUES 
('8144344192', 1234, 10000.00),
('7205010463', 1111, 5000.00)
ON DUPLICATE KEY UPDATE balance=VALUES(balance);

-- Show the inserted users
SELECT card_number, pin, balance FROM users;
