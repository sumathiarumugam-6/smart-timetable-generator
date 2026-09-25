-- ====================================================================================
-- Smart Automatic Timetable Generator for Engineering Colleges
-- Seed Data Script
-- ====================================================================================

USE timetable_db;

-- 1. Insert Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_SUPER_ADMIN', 'Super Administrator with full system access'),
(2, 'ROLE_ADMIN', 'Institutional Administrator'),
(3, 'ROLE_HOD', 'Head of Department'),
(4, 'ROLE_FACULTY', 'Teaching Faculty Member');

-- 2. Insert Departments
INSERT INTO departments (id, code, name, hod_name, building) VALUES
(1, 'CSE', 'Computer Science & Engineering', 'Dr. Aris Thorne', 'Turing Block'),
(2, 'ECE', 'Electronics & Communication Engg', 'Dr. Sarah Connor', 'Tesla Wing'),
(3, 'EEE', 'Electrical & Electronics Engg', 'Dr. Nikola Vance', 'Edison Hall'),
(4, 'MECH', 'Mechanical Engineering', 'Dr. Marcus Vance', 'Newton Block'),
(5, 'CIVIL', 'Civil Engineering', 'Dr. Elena Rostova', 'Visvesvaraya Hall');

-- 3. Insert Users (BCrypt Password for 'Password@123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2)
INSERT INTO users (id, username, password, email, full_name, role_id, department_id) VALUES
(1, 'superadmin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'admin@enggcollege.edu', 'System Super Admin', 1, 1),
(2, 'hod_cse', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'hod.cse@enggcollege.edu', 'Dr. Aris Thorne', 3, 1),
(3, 'hod_ece', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'hod.ece@enggcollege.edu', 'Dr. Sarah Connor', 3, 2),
(4, 'prof_john', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'john.doe@enggcollege.edu', 'Prof. John Doe', 4, 1),
(5, 'prof_alice', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'alice.smith@enggcollege.edu', 'Prof. Alice Smith', 4, 1);

-- 4. Insert Faculties
INSERT INTO faculties (id, user_id, faculty_code, name, designation, department_id, email, phone, max_working_hours, current_workload) VALUES
(1, 2, 'FAC_CSE_001', 'Dr. Aris Thorne', 'Professor & HOD', 1, 'hod.cse@enggcollege.edu', '+91 9876543210', 16, 12),
(2, 4, 'FAC_CSE_002', 'Prof. John Doe', 'Associate Professor', 1, 'john.doe@enggcollege.edu', '+91 9876543211', 20, 18),
(3, 5, 'FAC_CSE_003', 'Prof. Alice Smith', 'Assistant Professor', 1, 'alice.smith@enggcollege.edu', '+91 9876543212', 22, 16);
