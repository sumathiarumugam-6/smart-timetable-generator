from fastapi import APIRouter, HTTPException, Depends
from typing import List, Dict, Any
import json

from .database import get_connection, seed_demo_data, clear_all_data
from .models import (
    Room, RoomCreate,
    Subject, SubjectCreate,
    Staff, StaffCreate,
    ClassDetail, ClassCreate, ClassSubjectAllocation,
    TimetableGenerationResult, TimetableSlotResponse
)
from .scheduler import TimetableScheduler

router = APIRouter(prefix="/api")

# --- ROOMS ---
@router.get("/rooms", response_model=List[Room])
def get_rooms():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id, name, room_type, capacity FROM rooms ORDER BY room_type, name")
    rooms = [dict(row) for row in cursor.fetchall()]
    conn.close()
    return rooms

@router.post("/rooms", response_model=Room)
def create_room(room: RoomCreate):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO rooms (name, room_type, capacity) VALUES (?, ?, ?)",
            (room.name.strip(), room.room_type, room.capacity)
        )
        conn.commit()
        new_id = cursor.lastrowid
        conn.close()
        return {**room.dict(), "id": new_id}
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=f"Failed to create room: {str(e)}")

@router.delete("/rooms/{room_id}")
def delete_room(room_id: int):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM rooms WHERE id = ?", (room_id,))
    conn.commit()
    conn.close()
    return {"message": "Room deleted successfully"}

# --- SUBJECTS ---
@router.get("/subjects", response_model=List[Subject])
def get_subjects():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id, code, name, weekly_hours, requires_lab, color FROM subjects ORDER BY code")
    subjects = [dict(row) for row in cursor.fetchall()]
    for s in subjects:
        s["requires_lab"] = bool(s["requires_lab"])
    conn.close()
    return subjects

@router.post("/subjects", response_model=Subject)
def create_subject(subj: SubjectCreate):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO subjects (code, name, weekly_hours, requires_lab, color) VALUES (?, ?, ?, ?, ?)",
            (subj.code.strip().upper(), subj.name.strip(), subj.weekly_hours, int(subj.requires_lab), subj.color)
        )
        conn.commit()
        new_id = cursor.lastrowid
        conn.close()
        return {**subj.dict(), "id": new_id}
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=f"Failed to create subject: {str(e)}")

@router.delete("/subjects/{subject_id}")
def delete_subject(subject_id: int):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM subjects WHERE id = ?", (subject_id,))
    conn.commit()
    conn.close()
    return {"message": "Subject deleted successfully"}

# --- STAFF ---
@router.get("/staff", response_model=List[Staff])
def get_staff():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT s.id, s.name, s.email, s.subjects_json, s.weekly_max_load,
               COALESCE((SELECT SUM(hours_per_week) FROM class_subjects WHERE teacher_id = s.id), 0) as current_load
        FROM staff s
        ORDER BY s.name
    """)
    staff_list = []
    for row in cursor.fetchall():
        item = dict(row)
        item["subjects"] = json.loads(item["subjects_json"])
        staff_list.append(item)
    conn.close()
    return staff_list

@router.post("/staff", response_model=Staff)
def create_staff(staff_in: StaffCreate):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES (?, ?, ?, ?)",
            (staff_in.name.strip(), staff_in.email, json.dumps(staff_in.subjects), staff_in.weekly_max_load)
        )
        conn.commit()
        new_id = cursor.lastrowid
        conn.close()
        return {**staff_in.dict(), "id": new_id, "current_load": 0}
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=f"Failed to create staff member: {str(e)}")

@router.delete("/staff/{staff_id}")
def delete_staff(staff_id: int):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM staff WHERE id = ?", (staff_id,))
    conn.commit()
    conn.close()
    return {"message": "Staff member deleted successfully"}

# --- CLASSES & CURRICULUM ---
@router.get("/classes", response_model=List[ClassDetail])
def get_classes():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT c.id, c.name, c.room_id, r.name as room_name
        FROM classes c
        LEFT JOIN rooms r ON c.room_id = r.id
        ORDER BY c.name
    """)
    classes = [dict(row) for row in cursor.fetchall()]

    for c in classes:
        cursor.execute("""
            SELECT 
                cs.id,
                cs.subject_id,
                s.name as subject_name,
                s.code as subject_code,
                s.color as subject_color,
                s.requires_lab,
                cs.teacher_id,
                st.name as teacher_name,
                cs.hours_per_week
            FROM class_subjects cs
            JOIN subjects s ON cs.subject_id = s.id
            LEFT JOIN staff st ON cs.teacher_id = st.id
            WHERE cs.class_id = ?
            ORDER BY s.name
        """, (c["id"],))
        allocs = [dict(r) for r in cursor.fetchall()]
        for a in allocs:
            a["requires_lab"] = bool(a["requires_lab"])
        c["allocations"] = allocs
        c["total_hours"] = sum(a["hours_per_week"] for a in allocs)

    conn.close()
    return classes

@router.post("/classes")
def create_class(class_in: ClassCreate):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO classes (name, room_id) VALUES (?, ?)",
            (class_in.name.strip(), class_in.room_id)
        )
        new_class_id = cursor.lastrowid

        for alloc in class_in.allocations:
            cursor.execute(
                "INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, ?, ?)",
                (new_class_id, alloc.subject_id, alloc.teacher_id, alloc.hours_per_week)
            )

        conn.commit()
        conn.close()
        return {"id": new_class_id, "name": class_in.name}
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=f"Failed to create class: {str(e)}")

@router.delete("/classes/{class_id}")
def delete_class(class_id: int):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM classes WHERE id = ?", (class_id,))
    conn.commit()
    conn.close()
    return {"message": "Class deleted successfully"}

@router.post("/classes/{class_id}/allocations")
def add_class_allocation(class_id: int, alloc: ClassSubjectAllocation):
    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("""
            INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(class_id, subject_id) DO UPDATE SET
                teacher_id = excluded.teacher_id,
                hours_per_week = excluded.hours_per_week
        """, (class_id, alloc.subject_id, alloc.teacher_id, alloc.hours_per_week))
        conn.commit()
        conn.close()
        return {"message": "Subject allocation updated"}
    except Exception as e:
        conn.close()
        raise HTTPException(status_code=400, detail=str(e))

@router.delete("/classes/{class_id}/allocations/{subject_id}")
def remove_class_allocation(class_id: int, subject_id: int):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM class_subjects WHERE class_id = ? AND subject_id = ?", (class_id, subject_id))
    conn.commit()
    conn.close()
    return {"message": "Subject removed from class"}

# --- TIMETABLE GENERATION & RETRIEVAL ---
@router.post("/timetable/generate", response_model=TimetableGenerationResult)
def generate_timetable():
    scheduler = TimetableScheduler()
    result = scheduler.generate()
    return result

@router.get("/timetable/current", response_model=List[TimetableSlotResponse])
def get_current_timetable():
    scheduler = TimetableScheduler()
    return scheduler.get_current_timetable()

@router.post("/timetable/clear")
def clear_timetable():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM timetable_slots")
    conn.commit()
    conn.close()
    return {"message": "Generated timetable cleared"}

@router.get("/timetable/stats")
def get_timetable_stats():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) as count FROM timetable_slots")
    slot_count = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM classes")
    class_count = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM staff")
    staff_count = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM subjects")
    subject_count = cursor.fetchone()["count"]

    cursor.execute("SELECT COUNT(*) as count FROM rooms")
    room_count = cursor.fetchone()["count"]

    conn.close()
    return {
        "scheduled_slots": slot_count,
        "classes": class_count,
        "staff": staff_count,
        "subjects": subject_count,
        "rooms": room_count
    }

# --- DEMO SEEDING & RESET ---
@router.post("/seed-demo")
def seed_demo():
    seed_demo_data()
    return {"message": "Demo data populated successfully with realistic classes, teachers, subjects, and rooms!"}

@router.post("/clear-all")
def clear_all():
    clear_all_data()
    return {"message": "All database records have been reset."}
