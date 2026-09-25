-- ====================================================================================
-- Smart Automatic Timetable Generator for Engineering Colleges
-- Database Schema Script (MySQL 8.0+)
-- ====================================================================================

CREATE DATABASE IF NOT EXISTS timetable_db;
USE timetable_db;

-- Drop tables if exists in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS timetable_entries;
DROP TABLE IF EXISTS timetables;
DROP TABLE IF EXISTS faculty_subject_mapping;
DROP TABLE IF EXISTS batches;
DROP TABLE IF EXISTS classrooms;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS faculties;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------
-- Table: roles
-- -----------------------------------------------------
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- -----------------------------------------------------
-- Table: departments
-- -----------------------------------------------------
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    hod_name VARCHAR(100),
    building VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL,
    department_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table: faculties
-- -----------------------------------------------------
CREATE TABLE faculties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    faculty_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    designation VARCHAR(50) NOT NULL,
    department_id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    max_working_hours INT DEFAULT 20,
    current_workload INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table: subjects
-- -----------------------------------------------------
CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    subject_type ENUM('THEORY', 'LAB', 'CORE', 'ELECTIVE') NOT NULL DEFAULT 'THEORY',
    semester INT NOT NULL,
    weekly_periods INT DEFAULT 3,
    department_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table: classrooms
-- -----------------------------------------------------
CREATE TABLE classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    building VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    room_type ENUM('CLASSROOM', 'LABORATORY') NOT NULL DEFAULT 'CLASSROOM',
    department_id BIGINT,
    is_available TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table: batches
-- -----------------------------------------------------
CREATE TABLE batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    academic_year VARCHAR(20) NOT NULL,
    department_id BIGINT NOT NULL,
    section VARCHAR(10) NOT NULL,
    semester INT NOT NULL,
    student_count INT DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_batch (academic_year, department_id, section, semester)
);

-- -----------------------------------------------------
-- Table: faculty_subject_mapping
-- -----------------------------------------------------
CREATE TABLE faculty_subject_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    preferred_classroom_id BIGINT,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    FOREIGN KEY (preferred_classroom_id) REFERENCES classrooms(id) ON DELETE SET NULL,
    UNIQUE KEY unique_mapping (faculty_id, subject_id, batch_id)
);

-- -----------------------------------------------------
-- Table: timetables
-- -----------------------------------------------------
CREATE TABLE timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    department_id BIGINT NOT NULL,
    status ENUM('DRAFT', 'GENERATED', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    fitness_score DOUBLE DEFAULT 0.0,
    conflict_count INT DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table: timetable_entries
-- -----------------------------------------------------
CREATE TABLE timetable_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY') NOT NULL,
    period_number INT NOT NULL, -- 1 to 8
    time_slot VARCHAR(30) NOT NULL,
    batch_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    faculty_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    is_lab_session TINYINT(1) DEFAULT 0,
    lab_group VARCHAR(10),
    FOREIGN KEY (timetable_id) REFERENCES timetables(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    UNIQUE KEY unique_slot_batch (timetable_id, day_of_week, period_number, batch_id)
);

-- -----------------------------------------------------
-- Table: leave_requests
-- -----------------------------------------------------
CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    substitute_faculty_id BIGINT,
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE CASCADE,
    FOREIGN KEY (substitute_faculty_id) REFERENCES faculties(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- -----------------------------------------------------
-- Table: notifications
-- -----------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) DEFAULT 'INFO',
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Indexes for fast querying
CREATE INDEX idx_timetable_dept ON timetables(department_id, academic_year, semester);
CREATE INDEX idx_entry_faculty ON timetable_entries(faculty_id, day_of_week, period_number);
CREATE INDEX idx_entry_classroom ON timetable_entries(classroom_id, day_of_week, period_number);
CREATE INDEX idx_faculty_dept ON faculties(department_id);
CREATE INDEX idx_subject_dept ON subjects(department_id, semester);
