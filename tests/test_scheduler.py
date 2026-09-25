import unittest
import os
import sys
from collections import defaultdict

# Add parent directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from backend.database import init_db, seed_demo_data, clear_all_data, get_connection
from backend.scheduler import TimetableScheduler, NUM_DAYS, NUM_PERIODS

class TestTimetableScheduler(unittest.TestCase):
    def setUp(self):
        init_db()

    def test_demo_schedule_generation_and_clash_freedom(self):
        """Tests that the scheduler produces a completely clash-free schedule for the demo curriculum."""
        seed_demo_data()
        scheduler = TimetableScheduler()
        result = scheduler.generate()

        self.assertTrue(result["success"], f"Generation failed: {result.get('message')}, conflicts: {result.get('conflicts')}")
        timetable = result["timetable"]
        self.assertGreater(len(timetable), 0)

        # 1. Verify Class-Time exclusivity (No 2 subjects for same class at same day & period)
        class_slots = set()
        for slot in timetable:
            key = (slot["class_id"], slot["day"], slot["period"])
            self.assertNotIn(key, class_slots, f"Class clash found at {key}")
            class_slots.add(key)

        # 2. Verify Staff-Time exclusivity (No teacher in 2 classes at same day & period)
        staff_slots = set()
        for slot in timetable:
            if slot["staff_id"]:
                key = (slot["staff_id"], slot["day"], slot["period"])
                self.assertNotIn(key, staff_slots, f"Staff clash found for teacher {slot['staff_name']} at {key}")
                staff_slots.add(key)

        # 3. Verify Room-Time exclusivity (No room double-booked at same day & period)
        room_slots = set()
        for slot in timetable:
            if slot["room_id"]:
                key = (slot["room_id"], slot["day"], slot["period"])
                self.assertNotIn(key, room_slots, f"Room clash found for room {slot['room_name']} at {key}")
                room_slots.add(key)

        # 4. Verify Lab requirements (Subjects requiring lab must be placed in a lab room)
        conn = get_connection()
        try:
            c = conn.cursor()
            c.execute("SELECT id, room_type FROM rooms")
            room_types = {r["id"]: r["room_type"] for r in c.fetchall()}
        finally:
            conn.close()

        for slot in timetable:
            if slot["requires_lab"]:
                assigned_type = room_types.get(slot["room_id"])
                self.assertEqual(assigned_type, "lab", f"Subject {slot['subject_name']} requires lab but got {assigned_type}")

        # 5. Verify Daily Frequency Limit: No subject repeats on the same day if hours <= 6
        subj_daily_count = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))
        for slot in timetable:
            subj_daily_count[slot["class_id"]][slot["subject_id"]][slot["day"]] += 1

        for cid, subjects_map in subj_daily_count.items():
            for sid, days_map in subjects_map.items():
                for day, count in days_map.items():
                    self.assertLessEqual(count, 1, f"Subject {sid} in class {cid} repeated {count} times on day {day}")

    def test_overloaded_hours_conflict_detection(self):
        """Tests that exceeding 48 hours for a class is accurately caught and reported."""
        clear_all_data()
        conn = get_connection()
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO rooms (name, room_type) VALUES ('Room 101', 'classroom')")
            rid = cur.lastrowid
            cur.execute("INSERT INTO subjects (code, name, weekly_hours) VALUES ('CS101', 'Intro CS', 50)")
            sid = cur.lastrowid
            cur.execute("INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES ('Dr. Test', 'test@test.com', ?, 50)", (f"[{sid}]",))
            tid = cur.lastrowid
            cur.execute("INSERT INTO classes (name, room_id) VALUES ('Class Overload', ?)", (rid,))
            cid = cur.lastrowid
            cur.execute("INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, ?, 50)", (cid, sid, tid))
            conn.commit()
        finally:
            conn.close()

        scheduler = TimetableScheduler()
        result = scheduler.generate()

        self.assertFalse(result["success"])
        conflict_types = [c["type"] for c in result["conflicts"]]
        self.assertIn("hours_exceeded", conflict_types)

    def test_teacher_overload_conflict_detection(self):
        """Tests that exceeding teacher weekly max load is reported."""
        clear_all_data()
        conn = get_connection()
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO rooms (name, room_type) VALUES ('Room 101', 'classroom')")
            rid = cur.lastrowid
            cur.execute("INSERT INTO subjects (code, name, weekly_hours) VALUES ('CS101', 'Intro CS', 30)")
            sid = cur.lastrowid
            cur.execute("INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES ('Dr. Busy', 'busy@test.com', ?, 15)", (f"[{sid}]",))
            tid = cur.lastrowid
            cur.execute("INSERT INTO classes (name, room_id) VALUES ('Class A', ?)", (rid,))
            cid = cur.lastrowid
            cur.execute("INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, ?, 30)", (cid, sid, tid))
            conn.commit()
        finally:
            conn.close()

        scheduler = TimetableScheduler()
        result = scheduler.generate()

        self.assertFalse(result["success"])
        conflict_types = [c["type"] for c in result["conflicts"]]
        self.assertIn("teacher_overloaded", conflict_types)

    def test_missing_lab_conflict_detection(self):
        """Tests that a lab requirement with 0 lab rooms in database produces a clear conflict."""
        clear_all_data()
        conn = get_connection()
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO rooms (name, room_type) VALUES ('Room 101', 'classroom')")
            rid = cur.lastrowid
            cur.execute("INSERT INTO subjects (code, name, weekly_hours, requires_lab) VALUES ('CS102', 'Lab CS', 4, 1)")
            sid = cur.lastrowid
            cur.execute("INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES ('Dr. Lab', 'lab@test.com', ?, 20)", (f"[{sid}]",))
            tid = cur.lastrowid
            cur.execute("INSERT INTO classes (name, room_id) VALUES ('Class Lab', ?)", (rid,))
            cid = cur.lastrowid
            cur.execute("INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, ?, 4)", (cid, sid, tid))
            conn.commit()
        finally:
            conn.close()

        scheduler = TimetableScheduler()
        result = scheduler.generate()

        self.assertFalse(result["success"])
        conflict_types = [c["type"] for c in result["conflicts"]]
        self.assertIn("no_lab", conflict_types)

    def test_unqualified_teacher_conflict_detection(self):
        """Tests that an unassigned subject with no qualified staff reports no_teacher conflict."""
        clear_all_data()
        conn = get_connection()
        try:
            cur = conn.cursor()
            cur.execute("INSERT INTO rooms (name, room_type) VALUES ('Room 101', 'classroom')")
            rid = cur.lastrowid
            cur.execute("INSERT INTO subjects (code, name, weekly_hours) VALUES ('PHYS101', 'Quantum Physics', 4)")
            sid = cur.lastrowid
            # Teacher only knows subject 999 (not sid)
            cur.execute("INSERT INTO staff (name, email, subjects_json, weekly_max_load) VALUES ('Dr. Chem', 'chem@test.com', '[999]', 20)")
            cur.execute("INSERT INTO classes (name, room_id) VALUES ('Class Quantum', ?)", (rid,))
            cid = cur.lastrowid
            cur.execute("INSERT INTO class_subjects (class_id, subject_id, teacher_id, hours_per_week) VALUES (?, ?, NULL, 4)", (cid, sid))
            conn.commit()
        finally:
            conn.close()

        scheduler = TimetableScheduler()
        result = scheduler.generate()

        self.assertFalse(result["success"])
        conflict_types = [c["type"] for c in result["conflicts"]]
        self.assertIn("no_teacher", conflict_types)

if __name__ == "__main__":
    unittest.main()
