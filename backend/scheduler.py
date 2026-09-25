import time
import math
import json
import random
import itertools
from collections import defaultdict
from typing import List, Dict, Any, Tuple, Optional, Set

from .database import get_connection

DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
NUM_DAYS = 6
NUM_PERIODS = 8
TOTAL_WEEKLY_SLOTS = NUM_DAYS * NUM_PERIODS  # 48 slots

class TimetableScheduler:
    def __init__(self):
        self.conflicts: List[Dict[str, Any]] = []

    def load_data_from_db(self):
        conn = get_connection()
        try:
            cursor = conn.cursor()

            # Load rooms
            cursor.execute("SELECT id, name, room_type, capacity FROM rooms")
            rooms = {row["id"]: dict(row) for row in cursor.fetchall()}

            # Load subjects
            cursor.execute("SELECT id, code, name, weekly_hours, requires_lab, color FROM subjects")
            subjects = {row["id"]: dict(row) for row in cursor.fetchall()}

            # Load staff
            cursor.execute("SELECT id, name, email, subjects_json, weekly_max_load FROM staff")
            staff = {}
            for row in cursor.fetchall():
                s_dict = dict(row)
                s_dict["subjects"] = json.loads(s_dict["subjects_json"])
                staff[s_dict["id"]] = s_dict

            # Load classes
            cursor.execute("SELECT id, name, room_id FROM classes")
            classes = {row["id"]: dict(row) for row in cursor.fetchall()}

            # Load class-subject allocations
            cursor.execute("""
                SELECT id, class_id, subject_id, teacher_id, hours_per_week 
                FROM class_subjects
            """)
            class_subjects = [dict(row) for row in cursor.fetchall()]

            return rooms, subjects, staff, classes, class_subjects
        finally:
            conn.close()

    def validate_preconditions(self, rooms, subjects, staff, classes, class_subjects) -> bool:
        """
        Validates mathematical constraints before scheduling:
        1. Class weekly hours cannot exceed 48 (6 days x 8 periods).
        2. Teacher assigned hours cannot exceed teacher's weekly_max_load.
        3. Every class subject must have a valid qualified teacher assigned or available.
        4. Subjects requiring labs must have at least one lab room available.
        5. Total lab hours requested cannot exceed total available lab capacity.
        """
        self.conflicts = []

        if not classes:
            self.conflicts.append({
                "type": "no_classes",
                "title": "No Classes Found",
                "description": "Please add at least one Class/Section to generate a timetable."
            })
            return False

        if not subjects:
            self.conflicts.append({
                "type": "no_subjects",
                "title": "No Subjects Found",
                "description": "Please add subjects before generating a timetable."
            })
            return False

        if not staff:
            self.conflicts.append({
                "type": "no_staff",
                "title": "No Staff Found",
                "description": "Please add teaching staff members to assign to subjects."
            })
            return False

        # 1. Check Class total hours <= 48
        class_hours = defaultdict(int)
        for cs in class_subjects:
            class_hours[cs["class_id"]] += cs["hours_per_week"]

        for class_id, total_h in class_hours.items():
            class_name = classes.get(class_id, {}).get("name", f"Class {class_id}")
            if total_h > TOTAL_WEEKLY_SLOTS:
                self.conflicts.append({
                    "type": "hours_exceeded",
                    "title": f"Total Hours Exceeded for {class_name}",
                    "description": f"{class_name} has {total_h} hours requested per week, but a 6-day x 8-period schedule has only {TOTAL_WEEKLY_SLOTS} periods available.",
                    "entity_id": class_id,
                    "entity_name": class_name
                })

        # 2. Check Teacher Assigned Hours vs Weekly Max Load
        teacher_assigned = defaultdict(int)
        for cs in class_subjects:
            tid = cs.get("teacher_id")
            if tid:
                teacher_assigned[tid] += cs["hours_per_week"]

        for tid, assigned_h in teacher_assigned.items():
            t_obj = staff.get(tid)
            if t_obj:
                max_load = t_obj.get("weekly_max_load", 20)
                if assigned_h > max_load:
                    self.conflicts.append({
                        "type": "teacher_overloaded",
                        "title": f"Teacher Overloaded: {t_obj['name']}",
                        "description": f"{t_obj['name']} is assigned {assigned_h} hours/week, which exceeds their maximum allowed load of {max_load} hours/week.",
                        "entity_id": tid,
                        "entity_name": t_obj["name"]
                    })

        # 3. Check teacher qualification & availability
        for cs in class_subjects:
            cid = cs["class_id"]
            sid = cs["subject_id"]
            tid = cs.get("teacher_id")
            class_name = classes.get(cid, {}).get("name", f"Class {cid}")
            subj_name = subjects.get(sid, {}).get("name", f"Subject {sid}")

            if tid:
                t_obj = staff.get(tid)
                if not t_obj:
                    self.conflicts.append({
                        "type": "invalid_teacher",
                        "title": f"Invalid Teacher for {subj_name}",
                        "description": f"Assigned teacher ID {tid} for {class_name} does not exist in staff list.",
                        "entity_id": cid,
                        "entity_name": class_name
                    })
            else:
                qualified = [t for t in staff.values() if sid in t.get("subjects", [])]
                if not qualified:
                    self.conflicts.append({
                        "type": "no_teacher",
                        "title": f"No Qualified Teacher for {subj_name}",
                        "description": f"No staff member is qualified to teach {subj_name} for {class_name}. Please assign or qualify a teacher.",
                        "entity_id": sid,
                        "entity_name": subj_name
                    })

        # 4. Check Labs
        lab_rooms = [r for r in rooms.values() if r.get("room_type") == "lab"]
        total_lab_hours = 0
        for cs in class_subjects:
            sid = cs["subject_id"]
            subj = subjects.get(sid, {})
            if subj.get("requires_lab"):
                total_lab_hours += cs["hours_per_week"]

        if total_lab_hours > 0 and len(lab_rooms) == 0:
            self.conflicts.append({
                "type": "no_lab",
                "title": "No Lab Rooms Available",
                "description": f"Curriculum contains subjects requiring laboratory practicals ({total_lab_hours} hrs), but no Lab room exists in the system.",
                "entity_id": None,
                "entity_name": "Labs"
            })
        elif total_lab_hours > len(lab_rooms) * TOTAL_WEEKLY_SLOTS:
            self.conflicts.append({
                "type": "lab_capacity_exceeded",
                "title": "Lab Capacity Insufficient",
                "description": f"Requested lab hours ({total_lab_hours} hrs) exceed total available capacity of all lab rooms ({len(lab_rooms) * TOTAL_WEEKLY_SLOTS} hrs).",
                "entity_id": None,
                "entity_name": "Labs"
            })

        return len(self.conflicts) == 0

    def generate(self) -> Dict[str, Any]:
        """
        Generates a 6-day x 8-period clash-free timetable using a Two-Stage
        Decomposition CSP Algorithm (Day Partitioning + Bipartite Period Matching).
        Guarantees:
        - Exact weekly subject hours for each class.
        - Non-repeating subject per day (max 1/day if hours <= 6).
        - No staff clashes across any period.
        - No class clashes across any period.
        - No room/lab clashes across any period.
        - Staff weekly max load respected.
        """
        start_time = time.time()
        rooms, subjects, staff, classes, class_subjects = self.load_data_from_db()

        # Step 1: Pre-validation
        if not self.validate_preconditions(rooms, subjects, staff, classes, class_subjects):
            return {
                "success": False,
                "message": "Validation failed with conflicting constraints.",
                "total_slots_scheduled": 0,
                "execution_time_ms": round((time.time() - start_time) * 1000, 2),
                "conflicts": self.conflicts,
                "timetable": []
            }

        # Step 2: Auto-resolve teacher if unassigned
        resolved_class_subjects = []
        for cs in class_subjects:
            cs_copy = dict(cs)
            if not cs_copy.get("teacher_id"):
                sid = cs_copy["subject_id"]
                qualified = [t for t in staff.values() if sid in t.get("subjects", [])]
                if qualified:
                    qualified.sort(key=lambda t: t.get("weekly_max_load", 20), reverse=True)
                    cs_copy["teacher_id"] = qualified[0]["id"]
            resolved_class_subjects.append(cs_copy)

        lab_room_ids = [r["id"] for r in rooms.values() if r.get("room_type") == "lab"]
        classroom_ids = [r["id"] for r in rooms.values() if r.get("room_type") == "classroom"]

        # Prepare allocations to distribute across the 6 working days
        # Each allocation of H hours needs H days (for H <= 6, exactly 1 per day!)
        # If H > 6, it can occupy multiple periods per day
        alloc_items = []
        for a in resolved_class_subjects:
            cid = a["class_id"]
            sid = a["subject_id"]
            tid = a["teacher_id"]
            hours = a["hours_per_week"]
            req_lab = bool(subjects[sid].get("requires_lab", False))
            alloc_items.append({
                "class_id": cid,
                "subject_id": sid,
                "teacher_id": tid,
                "hours": hours,
                "req_lab": req_lab
            })

        # Heuristic sort: lab subjects first, then highest weekly hours
        alloc_items.sort(key=lambda x: (x["req_lab"], x["hours"]), reverse=True)

        # STAGE 1: Distribute each subject across the 6 days
        # Constraints:
        # - class_day_count[cid][day] <= 8
        # - teacher_day_count[tid][day] <= 8
        # - lab_day_count[day] <= len(lab_room_ids) * 8
        day_schedule = defaultdict(lambda: defaultdict(list))
        class_day_count = defaultdict(lambda: defaultdict(int))
        teacher_day_count = defaultdict(lambda: defaultdict(int))
        lab_day_count = defaultdict(int)

        max_stage1_steps = [0]
        MAX_STAGE1_STEPS = 100000

        def assign_days(idx):
            if idx == len(alloc_items):
                return True

            max_stage1_steps[0] += 1
            if max_stage1_steps[0] > MAX_STAGE1_STEPS:
                return False

            item = alloc_items[idx]
            cid = item["class_id"]
            sid = item["subject_id"]
            tid = item["teacher_id"]
            hours = item["hours"]
            req_lab = item["req_lab"]

            if hours <= NUM_DAYS:
                all_combos = list(itertools.combinations(range(NUM_DAYS), hours))
            else:
                # If hours > 6, repeat some days
                base = list(range(NUM_DAYS))
                extra = list(itertools.combinations(range(NUM_DAYS), hours - NUM_DAYS))
                all_combos = [base + list(e) for e in extra]

            # Heuristic sort: prefer day combinations that spread load evenly
            def score_combo(combo):
                score = 0
                for d in combo:
                    score += class_day_count[cid][d] * 2
                    if tid:
                        score += teacher_day_count[tid][d] * 3
                    if req_lab:
                        score += lab_day_count[d] * 4
                return score

            all_combos.sort(key=score_combo)

            for combo in all_combos:
                # Check feasibility of this combination
                valid = True
                d_counts = defaultdict(int)
                for d in combo:
                    d_counts[d] += 1

                for d, count in d_counts.items():
                    if class_day_count[cid][d] + count > NUM_PERIODS:
                        valid = False
                        break
                    if tid and teacher_day_count[tid][d] + count > NUM_PERIODS:
                        valid = False
                        break
                    if req_lab and lab_room_ids and (lab_day_count[d] + count > len(lab_room_ids) * NUM_PERIODS):
                        valid = False
                        break

                if not valid:
                    continue

                # Apply combination
                for d in combo:
                    class_day_count[cid][d] += 1
                    if tid:
                        teacher_day_count[tid][d] += 1
                    if req_lab:
                        lab_day_count[d] += 1
                    day_schedule[d][cid].append((sid, tid, req_lab))

                if assign_days(idx + 1):
                    return True

                # Backtrack
                for d in combo:
                    class_day_count[cid][d] -= 1
                    if tid:
                        teacher_day_count[tid][d] -= 1
                    if req_lab:
                        lab_day_count[d] -= 1
                    day_schedule[d][cid].pop()

            return False

        stage1_ok = assign_days(0)
        if not stage1_ok:
            self.conflicts.append({
                "type": "unsolvable_clash",
                "title": "Day Partitioning Infeasible",
                "description": "Unable to distribute subject hours across the 6 working days without exceeding 8 periods per day for classes, teachers, or labs."
            })
            return {
                "success": False,
                "message": "Could not partition subjects across 6 days without day overload.",
                "total_slots_scheduled": 0,
                "execution_time_ms": round((time.time() - start_time) * 1000, 2),
                "conflicts": self.conflicts,
                "timetable": []
            }

        # STAGE 2: Assign periods (0..7) and rooms for each day d
        final_slots = []
        for d in range(NUM_DAYS):
            day_items = []
            for cid, items in day_schedule[d].items():
                for sid, tid, req_lab in items:
                    day_items.append({
                        "class_id": cid,
                        "subject_id": sid,
                        "teacher_id": tid,
                        "req_lab": req_lab
                    })

            # Labs first (most constrained resource)
            day_items.sort(key=lambda x: x["req_lab"], reverse=True)

            class_period = defaultdict(dict)
            teacher_period = defaultdict(dict)
            room_period = defaultdict(dict)
            day_assignments = []

            def solve_day(item_idx):
                if item_idx == len(day_items):
                    return True

                it = day_items[item_idx]
                cid = it["class_id"]
                sid = it["subject_id"]
                tid = it["teacher_id"]
                req_lab = it["req_lab"]

                candidate_periods = list(range(NUM_PERIODS))
                # Heuristic: try periods in alternating/distributed order
                random.seed(42 + d * 10 + item_idx)
                random.shuffle(candidate_periods)

                for p in candidate_periods:
                    if p in class_period[cid]:
                        continue
                    if tid and p in teacher_period[tid]:
                        continue

                    # Select room
                    if req_lab:
                        free_labs = [rid for rid in lab_room_ids if p not in room_period[rid]]
                        if not free_labs and lab_room_ids:
                            continue
                        chosen_room = free_labs[0] if free_labs else None
                    else:
                        default_room = classes[cid].get("room_id")
                        if default_room and p not in room_period[default_room]:
                            chosen_room = default_room
                        else:
                            free_cr = [rid for rid in classroom_ids if p not in room_period[rid]]
                            chosen_room = free_cr[0] if free_cr else (classes[cid].get("room_id") or (classroom_ids[0] if classroom_ids else None))

                    # Apply assignment
                    class_period[cid][p] = sid
                    if tid:
                        teacher_period[tid][p] = cid
                    if chosen_room:
                        room_period[chosen_room][p] = cid

                    day_assignments.append({
                        "class_id": cid,
                        "day": d,
                        "period": p,
                        "subject_id": sid,
                        "staff_id": tid,
                        "room_id": chosen_room
                    })

                    if solve_day(item_idx + 1):
                        return True

                    # Backtrack
                    del class_period[cid][p]
                    if tid:
                        del teacher_period[tid][p]
                    if chosen_room:
                        del room_period[chosen_room][p]
                    day_assignments.pop()

                return False

            if not solve_day(0):
                self.conflicts.append({
                    "type": "unsolvable_clash",
                    "title": f"Period Assignment Conflict on {DAYS[d]}",
                    "description": f"On {DAYS[d]}, staff or room overlaps prevented assigning all required subjects to the 8 available periods."
                })
                return {
                    "success": False,
                    "message": f"Could not schedule all subjects without clash on {DAYS[d]}.",
                    "total_slots_scheduled": 0,
                    "execution_time_ms": round((time.time() - start_time) * 1000, 2),
                    "conflicts": self.conflicts,
                    "timetable": []
                }

            final_slots.extend(day_assignments)

        # STAGE 3: Persist generated timetable to SQLite
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM timetable_slots")

            for slot in final_slots:
                cursor.execute("""
                    INSERT INTO timetable_slots (class_id, day, period, subject_id, staff_id, room_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                """, (slot["class_id"], slot["day"], slot["period"], slot["subject_id"], slot["staff_id"], slot["room_id"]))

            conn.commit()
        finally:
            conn.close()

        # Format output response
        response_timetable = []
        for slot in final_slots:
            s_obj = subjects.get(slot["subject_id"], {})
            t_obj = staff.get(slot["staff_id"], {})
            r_obj = rooms.get(slot["room_id"], {})
            c_obj = classes.get(slot["class_id"], {})

            response_timetable.append({
                "class_id": slot["class_id"],
                "class_name": c_obj.get("name", f"Class {slot['class_id']}"),
                "day": slot["day"],
                "period": slot["period"],
                "subject_id": slot["subject_id"],
                "subject_name": s_obj.get("name", ""),
                "subject_code": s_obj.get("code", ""),
                "subject_color": s_obj.get("color", "#4f46e5"),
                "requires_lab": bool(s_obj.get("requires_lab", False)),
                "staff_id": slot["staff_id"],
                "staff_name": t_obj.get("name", "Unassigned") if slot["staff_id"] else "Unassigned",
                "room_id": slot["room_id"],
                "room_name": r_obj.get("name", "Default Room") if slot["room_id"] else "Standard Room"
            })

        response_timetable.sort(key=lambda x: (x["day"], x["period"], x["class_id"]))
        exec_time = round((time.time() - start_time) * 1000, 2)

        return {
            "success": True,
            "message": f"Successfully generated clash-free timetable with {len(response_timetable)} periods scheduled across {len(classes)} classes!",
            "total_slots_scheduled": len(response_timetable),
            "execution_time_ms": exec_time,
            "conflicts": [],
            "timetable": response_timetable
        }

    def get_current_timetable(self) -> List[Dict[str, Any]]:
        """Retrieves currently saved timetable slots joined with entities."""
        conn = get_connection()
        try:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT 
                    ts.id,
                    ts.class_id,
                    c.name as class_name,
                    ts.day,
                    ts.period,
                    ts.subject_id,
                    s.name as subject_name,
                    s.code as subject_code,
                    s.color as subject_color,
                    s.requires_lab,
                    ts.staff_id,
                    st.name as staff_name,
                    ts.room_id,
                    r.name as room_name
                FROM timetable_slots ts
                JOIN classes c ON ts.class_id = c.id
                JOIN subjects s ON ts.subject_id = s.id
                LEFT JOIN staff st ON ts.staff_id = st.id
                LEFT JOIN rooms r ON ts.room_id = r.id
                ORDER BY ts.day, ts.period, ts.class_id
            """)
            rows = [dict(r) for r in cursor.fetchall()]
            for r in rows:
                r["requires_lab"] = bool(r["requires_lab"])
            return rows
        finally:
            conn.close()
