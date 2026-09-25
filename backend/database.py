import sqlite3
import json
import os
from typing import List, Dict, Any, Optional

DB_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "timetable.db")

def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn

def init_db():
    conn = get_connection()
    cursor = conn.cursor()

    cursor.executescript("""
    CREATE TABLE IF NOT EXISTS rooms (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE,
        room_type TEXT NOT NULL DEFAULT 'classroom' CHECK (room_type IN ('classroom', 'lab')),
        capacity INTEGER DEFAULT 60
    );

    CREATE TABLE IF NOT EXISTS subjects (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        code TEXT NOT NULL UNIQUE,
        name TEXT NOT NULL,
        weekly_hours INTEGER NOT NULL DEFAULT 4,
        requires_lab INTEGER NOT NULL DEFAULT 0,
        color TEXT DEFAULT '#4f46e5'
    );

    CREATE TABLE IF NOT EXISTS staff (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        email TEXT UNIQUE,
        subjects_json TEXT NOT NULL DEFAULT '[]', -- JSON array of subject IDs they can teach
        weekly_max_load INTEGER NOT NULL DEFAULT 20
    );

    CREATE TABLE IF NOT EXISTS classes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE,
        room_id INTEGER,
        FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
    );

    CREATE TABLE IF NOT EXISTS class_subjects (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        class_id INTEGER NOT NULL,
        subject_id INTEGER NOT NULL,
        teacher_id INTEGER,
        hours_per_week INTEGER NOT NULL DEFAULT 4,
        FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
        FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE SET NULL,
        UNIQUE(class_id, subject_id)
    );

    CREATE TABLE IF NOT EXISTS timetable_slots (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        class_id INTEGER NOT NULL,
        day INTEGER NOT NULL CHECK (day BETWEEN 0 AND 5), -- 0: Monday, 5: Saturday
        period INTEGER NOT NULL CHECK (period BETWEEN 0 AND 7), -- 0: Period 1, 7: Period 8
        subject_id INTEGER NOT NULL,
        staff_id INTEGER NOT NULL,
        room_id INTEGER,
        FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
        FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE,
        FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
        UNIQUE(class_id, day, period)
    );
    """)
    conn.commit()
    conn.close()

def clear_all_data():
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.executescript("""
        DELETE FROM timetable_slots;
        DELETE FROM class_subjects;
        DELETE FROM classes;
        DELETE FROM staff;
        DELETE FROM subjects;
        DELETE FROM rooms;
        DELETE FROM sqlite_sequence WHERE name IN ('rooms', 'subjects', 'staff', 'classes', 'class_subjects', 'timetable_slots');
        """)
        conn.commit()
    finally:
        conn.close()

def seed_demo_data():
    """Populates realistic high school / undergraduate curriculum data."""
    init_db()
    clear_all_data()
    conn = get_connection()
    try:
        cursor = conn.cursor()

        # 1. Rooms (3 regular classrooms + 3 specialized labs)
        rooms = [
            ("Room 101", "classroom", 60),
            ("Room 102", "classroom", 60),
            ("Room 103", "classroom", 60),
            ("Computer Lab 1", "lab", 40),
            ("Computer Lab 2", "lab", 40),
            ("Physics Lab", "lab", 40)
        ]
        cursor.executemany("INSERT INTO rooms (name, room_type, capacity) VALUES (?, ?, ?)", rooms)

        # 2. Subjects with visually distinct colors
        subjects = [
            ("CS101", "Data Structures", 6, 0, "#3b82f6"),       # Blue
            ("CS102", "Data Structures Lab", 4, 1, "#06b6d4"),   # Cyan (requires lab)
            ("MA101", "Discrete Mathematics", 6, 0, "#8b5cf6"),  # Purple
            ("CS103", "Database Systems", 6, 0, "#10b981"),      # Emerald
            ("CS104", "Database Systems Lab", 4, 1, "#14b8a6"),  # Teal (requires lab)
            ("EC101", "Digital Electronics", 6, 0, "#f59e0b"),   # Amber
            ("CS105", "Operating Systems", 6, 0, "#ec4899"),     # Pink
            ("HU101", "Technical Communication", 5, 0, "#6366f1"), # Indigo
            ("AI101", "Machine Learning", 5, 0, "#ef4444")        # Red
        ]
        cursor.executemany("INSERT INTO subjects (code, name, weekly_hours, requires_lab, color) VALUES (?, ?, ?, ?, ?)", subjects)

        # 3. Staff members with realistic maximum weekly teaching hours
        staff_members = [
            ("Dr. Alan Turing", "turing@cs.edu", [1, 2, 9], 24),         # DS, DS Lab, ML
            ("Prof. Ada Lovelace", "ada@cs.edu", [1, 3, 7], 24),          # DS, Math, OS
            ("Dr. Grace Hopper", "hopper@cs.edu", [4, 5, 7], 24),        # DB, DB Lab, OS
            ("Prof. John von Neumann", "neumann@math.edu", [3, 6], 24),   # Math, Digital Electronics
            ("Prof. Claude Shannon", "shannon@ec.edu", [6, 9], 25),       # Digital Electronics, ML (23h needed)
            ("Dr. Barbara Liskov", "liskov@cs.edu", [4, 5, 1], 24),       # DB, DB Lab, DS
            ("Prof. Linus Torvalds", "linus@os.edu", [7, 2], 24),         # OS, DS Lab
            ("Dr. Maya Angelou", "maya@hum.edu", [8], 20)                 # Tech Comm
        ]
        cursor.executemany(
            "INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES (?, ?, ?, ?)",
            [(s[0], s[1], json.dumps(s[2]), s[3]) for s in staff_members]
        )

        # 4. Classes (linked to default classrooms)
        classes = [
            ("CSE - Year 2 (Sec A)", 1),
            ("CSE - Year 2 (Sec B)", 2),
            ("CSE - Year 3 (Sec A)", 3)
        ]
        cursor.executemany("INSERT INTO classes (name, room_id) VALUES (?, ?)", classes)

        # 5. Class-Subject Assignments
        # Sec A (Year 2): Total = 6+4+6+6+4+6+6+5+5 = 48 periods!
        allocations_sec_a = [
            (1, 1, 1, 6),  # DS: Turing (6h)
            (1, 2, 7, 4),  # DS Lab: Linus (4h)
            (1, 3, 4, 6),  # Math: Neumann (6h)
            (1, 4, 3, 6),  # DBMS: Hopper (6h)
            (1, 5, 6, 4),  # DBMS Lab: Liskov (4h)
            (1, 6, 5, 6),  # Dig. Elec: Shannon (6h)
            (1, 7, 2, 6),  # OS: Ada (6h)
            (1, 8, 8, 5),  # Tech Comm: Maya (5h)
            (1, 9, 1, 5),  # ML: Turing (5h)
        ]

        # Sec B (Year 2): Total = 48 periods!
        allocations_sec_b = [
            (2, 1, 2, 6),  # DS: Ada (6h)
            (2, 2, 1, 4),  # DS Lab: Turing (4h)
            (2, 3, 4, 6),  # Math: Neumann (6h)
            (2, 4, 6, 6),  # DBMS: Liskov (6h)
            (2, 5, 3, 4),  # DBMS Lab: Hopper (4h)
            (2, 6, 5, 6),  # Dig. Elec: Shannon (6h)
            (2, 7, 7, 6),  # OS: Linus (6h)
            (2, 8, 8, 5),  # Tech Comm: Maya (5h)
            (2, 9, 5, 5),  # ML: Shannon (5h)
        ]

        # Sec A (Year 3): Total = 44 periods! (leaves 4 free periods)
        allocations_sec_c = [
            (3, 1, 6, 6),  # DS: Liskov (6h)
            (3, 2, 7, 4),  # DS Lab: Linus (4h)
            (3, 3, 4, 6),  # Math: Neumann (6h)
            (3, 4, 3, 6),  # DBMS: Hopper (6h)
            (3, 5, 6, 4),  # DBMS Lab: Liskov (4h)
            (3, 6, 5, 6),  # Dig. Elec: Shannon (6h)
            (3, 7, 2, 6),  # OS: Ada (6h)
            (3, 8, 8, 6),  # Tech Comm: Maya (6h)
        ]

        all_allocs = allocations_sec_a + allocations_sec_b + allocations_sec_c
        cursor.executemany(
            "INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, ?, ?)",
            all_allocs
        )

        conn.commit()
    finally:
        conn.close()
