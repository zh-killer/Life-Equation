-- ============================================================
-- H2 测试数据库 Schema（MySQL 兼容模式）
-- 仅包含测试需要的核心表
-- ============================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(50),
    `role` VARCHAR(20) DEFAULT 'USER',
    `phone` VARCHAR(20),
    `email` VARCHAR(100),
    `avatar_url` VARCHAR(500),
    `gender` TINYINT DEFAULT 0,
    `birth_date` DATE,
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `hospital` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(255),
    `city` VARCHAR(50),
    `phone` VARCHAR(20),
    `level` VARCHAR(20),
    `introduction` TEXT,
    `departments` VARCHAR(500),
    `image_url` VARCHAR(500),
    `business_hours` VARCHAR(100),
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `doctor` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `hospital_id` BIGINT,
    `name` VARCHAR(50) NOT NULL,
    `title` VARCHAR(50),
    `specialty` VARCHAR(255),
    `department` VARCHAR(50),
    `introduction` TEXT,
    `avatar_url` VARCHAR(500),
    `consultation_fee` DECIMAL(10,2) DEFAULT 0.00,
    `is_available` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `appointment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `doctor_id` BIGINT,
    `hospital_id` BIGINT,
    `appointment_date` DATE,
    `time_slot` VARCHAR(20),
    `symptom_desc` VARCHAR(500),
    `status` VARCHAR(20) DEFAULT 'pending',
    `remark` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `ai_consultation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `symptoms_input` TEXT,
    `ai_analysis` TEXT,
    `suggested_department` VARCHAR(50),
    `risk_level` VARCHAR(20),
    `health_advice` TEXT,
    `related_diseases` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `ai_health_advice` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `age` INT,
    `height` DECIMAL(5,2),
    `weight` DECIMAL(5,2),
    `gender` VARCHAR(10),
    `lifestyle` VARCHAR(500),
    `medical_history` TEXT,
    `concerns` TEXT,
    `ai_advice` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `ai_report_interpretation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `report_type` VARCHAR(50),
    `image_url` VARCHAR(500),
    `ocr_text` TEXT,
    `ai_interpretation` TEXT,
    `key_findings` TEXT,
    `risk_assessment` VARCHAR(500),
    `recommendations` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);
