-- ====================================================================================
-- Smart Automatic Timetable Generator for Engineering Colleges
-- Seed Data Script (Valid BCrypt Hash for 'Password@123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2)
-- ====================================================================================

-- 1. Insert Roles
MERGE INTO roles (id, name, description) KEY(id) VALUES
(1, 'ROLE_SUPER_ADMIN', 'Super Administrator with full system access'),
(2, 'ROLE_ADMIN', 'Institutional Administrator'),
(3, 'ROLE_HOD', 'Head of Department'),
(4, 'ROLE_FACULTY', 'Teaching Faculty Member');

-- 2. Insert Departments
MERGE INTO departments (id, code, name, hod_name, building) KEY(id) VALUES
(1, 'CSE', 'Computer Science & Engineering', 'Dr. Aris Thorne', 'Turing Block'),
(2, 'ECE', 'Electronics & Communication Engg', 'Dr. Sarah Connor', 'Tesla Wing'),
(3, 'EEE', 'Electrical & Electronics Engg', 'Dr. Nikola Vance', 'Edison Hall'),
(4, 'MECH', 'Mechanical Engineering', 'Dr. Marcus Vance', 'Newton Block'),
(5, 'CIVIL', 'Civil Engineering', 'Dr. Elena Rostova', 'Visvesvaraya Hall');

-- 3. Insert Users (BCrypt Hash for 'Password@123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2)
MERGE INTO users (id, username, password, email, full_name, role_id, department_id) KEY(id) VALUES
(1, 'superadmin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'admin@enggcollege.edu', 'System Super Admin', 1, 1),
(2, 'hod_cse', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'hod.cse@enggcollege.edu', 'Dr. Aris Thorne', 3, 1),
(3, 'hod_ece', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'hod.ece@enggcollege.edu', 'Dr. Sarah Connor', 3, 2),
(4, 'prof_john', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'john.doe@enggcollege.edu', 'Prof. John Doe', 4, 1),
(5, 'prof_alice', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY0dqxs61LBLgUfR51aJ2', 'alice.smith@enggcollege.edu', 'Prof. Alice Smith', 4, 1);

-- 4. Insert Faculties
MERGE INTO faculties (id, user_id, faculty_code, name, designation, department_id, email, phone, max_working_hours, current_workload) KEY(id) VALUES
(1, 2, 'FAC_CSE_001', 'Dr. Aris Thorne', 'Professor & HOD', 1, 'hod.cse@enggcollege.edu', '+91 9876543210', 16, 12),
(2, 4, 'FAC_CSE_002', 'Prof. John Doe', 'Associate Professor', 1, 'john.doe@enggcollege.edu', '+91 9876543211', 20, 18),
(3, 5, 'FAC_CSE_003', 'Prof. Alice Smith', 'Assistant Professor', 1, 'alice.smith@enggcollege.edu', '+91 9876543212', 22, 16),
(10, NULL, 'FAC_EEE_001', 'Dr. Nikola Vance', 'Professor & HOD', 3, 'hod.eee@enggcollege.edu', '+91 9876543220', 16, 12),
(11, NULL, 'FAC_EEE_002', 'Prof. Michael Faraday', 'Associate Professor', 3, 'faraday.eee@enggcollege.edu', '+91 9876543221', 20, 16),
(12, NULL, 'FAC_EEE_003', 'Prof. Thomas Edison', 'Assistant Professor', 3, 'edison.eee@enggcollege.edu', '+91 9876543222', 22, 14),
(20, NULL, 'FAC_MECH_001', 'Dr. Marcus Vance', 'Professor & HOD', 4, 'hod.mech@enggcollege.edu', '+91 9876543230', 16, 12),
(21, NULL, 'FAC_MECH_002', 'Prof. James Watt', 'Associate Professor', 4, 'watt.mech@enggcollege.edu', '+91 9876543231', 20, 16),
(22, NULL, 'FAC_MECH_003', 'Prof. Rudolf Diesel', 'Assistant Professor', 4, 'diesel.mech@enggcollege.edu', '+91 9876543232', 22, 14),
(30, NULL, 'FAC_CIVIL_001', 'Dr. Elena Rostova', 'Professor & HOD', 5, 'hod.civil@enggcollege.edu', '+91 9876543240', 16, 12),
(31, NULL, 'FAC_CIVIL_002', 'Prof. Arthur Casagrande', 'Associate Professor', 5, 'casagrande.civil@enggcollege.edu', '+91 9876543241', 20, 16),
(32, NULL, 'FAC_CIVIL_003', 'Prof. M. Visvesvaraya', 'Assistant Professor', 5, 'visvesvaraya.civil@enggcollege.edu', '+91 9876543242', 22, 14);

-- 5. Insert Subjects
MERGE INTO subjects (id, subject_code, subject_name, credits, subject_type, semester, weekly_periods, department_id) KEY(id) VALUES
(1, 'CS501', 'Data Structures & Algorithms', 4, 'CORE', 5, 4, 1),
(2, 'CS502', 'Database Management Systems', 4, 'CORE', 5, 4, 1),
(3, 'CS503', 'Operating Systems', 3, 'THEORY', 5, 3, 1),
(4, 'CS504L', 'DBMS Laboratory', 2, 'LAB', 5, 3, 1),
(10, 'EE501', 'Electric Circuit Analysis', 4, 'CORE', 5, 4, 3),
(11, 'EE502', 'Electrical Machines II', 4, 'CORE', 5, 4, 3),
(12, 'EE503', 'Power Electronics', 3, 'THEORY', 5, 3, 3),
(13, 'EE504L', 'Electrical Machines & Simulation Lab', 2, 'LAB', 5, 3, 3),
(20, 'ME501', 'Thermodynamics & Heat Transfer', 4, 'CORE', 5, 4, 4),
(21, 'ME502', 'Mechanics of Solids', 4, 'CORE', 5, 4, 4),
(22, 'ME503', 'Fluid Power Systems', 3, 'THEORY', 5, 3, 4),
(23, 'ME504L', 'CAD/CAM & Fluid Machinery Lab', 2, 'LAB', 5, 3, 4),
(30, 'CE501', 'Structural Analysis', 4, 'CORE', 5, 4, 5),
(31, 'CE502', 'Geotechnical Engineering', 4, 'CORE', 5, 4, 5),
(32, 'CE503', 'Surveying & Geomatics', 3, 'THEORY', 5, 3, 5),
(33, 'CE504L', 'Concrete & Highway Engineering Lab', 2, 'LAB', 5, 3, 5);

-- 6. Insert Classrooms & Labs
MERGE INTO classrooms (id, room_number, building, capacity, room_type, department_id) KEY(id) VALUES
(1, 'TB-101', 'Turing Block', 70, 'CLASSROOM', 1),
(2, 'TB-102', 'Turing Block', 70, 'CLASSROOM', 1),
(3, 'TB-LAB1', 'Turing Block', 40, 'LABORATORY', 1),
(10, 'EH-201', 'Edison Hall', 70, 'CLASSROOM', 3),
(11, 'EH-202', 'Edison Hall', 70, 'CLASSROOM', 3),
(12, 'EH-LAB1', 'Edison Hall', 40, 'LABORATORY', 3),
(20, 'NB-101', 'Newton Block', 70, 'CLASSROOM', 4),
(21, 'NB-102', 'Newton Block', 70, 'CLASSROOM', 4),
(22, 'NB-LAB1', 'Newton Block', 40, 'LABORATORY', 4),
(30, 'VH-101', 'Visvesvaraya Hall', 70, 'CLASSROOM', 5),
(31, 'VH-102', 'Visvesvaraya Hall', 70, 'CLASSROOM', 5),
(32, 'VH-LAB1', 'Visvesvaraya Hall', 40, 'LABORATORY', 5);

-- 7. Insert Batches
MERGE INTO batches (id, academic_year, department_id, section, semester, student_count) KEY(id) VALUES
(1, '2025-2026', 1, 'A', 5, 60),
(2, '2025-2026', 1, 'B', 5, 58),
(10, '2025-2026', 3, 'A', 5, 60),
(20, '2025-2026', 4, 'A', 5, 60),
(30, '2025-2026', 5, 'A', 5, 60);

-- 8. Faculty Subject Mapping
MERGE INTO faculty_subject_mapping (id, faculty_id, subject_id, batch_id, preferred_classroom_id) KEY(id) VALUES
(1, 1, 1, 1, 1),
(2, 2, 2, 1, 1),
(3, 3, 3, 1, 1),
(4, 2, 4, 1, 3),
(10, 10, 10, 10, 10),
(11, 11, 11, 10, 10),
(12, 12, 12, 10, 10),
(13, 11, 13, 10, 12),
(20, 20, 20, 20, 20),
(21, 21, 21, 20, 20),
(22, 22, 22, 20, 20),
(23, 21, 23, 20, 22),
(30, 30, 30, 30, 30),
(31, 31, 31, 30, 30),
(32, 32, 32, 30, 30),
(33, 31, 33, 30, 32);
