import os
import sys
import json
import time
from collections import defaultdict
import random

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from backend.database import get_connection, seed_demo_data

seed_demo_data()
conn = get_connection()
c = conn.cursor()
c.execute("SELECT id, name, room_id FROM classes")
classes = {r["id"]: dict(r) for r in c.fetchall()}

c.execute("SELECT id, name, subjects_json, weekly_max_load FROM staff")
staff = {}
for r in c.fetchall():
    d = dict(r)
    d["subjects"] = json.loads(d["subjects_json"])
    staff[d["id"]] = d

c.execute("SELECT id, code, name, weekly_hours, requires_lab, color FROM subjects")
subjects = {r["id"]: dict(r) for r in c.fetchall()}

c.execute("SELECT id, name, room_type FROM rooms")
rooms = {r["id"]: dict(r) for r in c.fetchall()}

c.execute("SELECT id, class_id, subject_id, teacher_id, hours_per_week FROM class_subjects")
allocs = [dict(r) for r in c.fetchall()]
conn.close()

NUM_DAYS = 6
NUM_PERIODS = 8
DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]

lab_room_ids = [r["id"] for r in rooms.values() if r.get("room_type") == "lab"]
classroom_ids = [r["id"] for r in rooms.values() if r.get("room_type") == "classroom"]

start_time = time.time()

# STAGE 1: Distribute subject allocations across Days 0..5
# Each subject with hours H must be placed on H distinct days.
# Ensure for each day d:
# - class has <= 8 subjects
# - teacher has <= 8 subjects
# - total lab subjects across all classes on day d <= len(lab_room_ids) * 8

day_schedule = defaultdict(lambda: defaultdict(list)) # day -> class_id -> list of (subject_id, teacher_id, req_lab)

# Prepare items to assign: for each class-subject allocation of hours H
alloc_items = []
for a in allocs:
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

# Sort items: lab first, then highest hours, then teacher load
alloc_items.sort(key=lambda x: (x["req_lab"], x["hours"]), reverse=True)

class_day_count = defaultdict(lambda: defaultdict(int))
teacher_day_count = defaultdict(lambda: defaultdict(int))
lab_day_count = defaultdict(int)

def assign_days(idx):
    if idx == len(alloc_items):
        return True
    item = alloc_items[idx]
    cid = item["class_id"]
    sid = item["subject_id"]
    tid = item["teacher_id"]
    hours = item["hours"]
    req_lab = item["req_lab"]

    # Choose `hours` distinct days from 0..5
    import itertools
    all_combos = list(itertools.combinations(range(NUM_DAYS), hours))
    # Heuristic sort combinations: prefer combos that balance teacher and class daily count
    def score_combo(combo):
        score = 0
        for d in combo:
            score += class_day_count[cid][d]
            if tid:
                score += teacher_day_count[tid][d]
            if req_lab:
                score += lab_day_count[d]
        return score

    all_combos.sort(key=score_combo)

    for combo in all_combos:
        # Check validity of combo
        valid = True
        for d in combo:
            if class_day_count[cid][d] >= NUM_PERIODS:
                valid = False; break
            if tid and teacher_day_count[tid][d] >= NUM_PERIODS:
                valid = False; break
            if req_lab and lab_day_count[d] >= len(lab_room_ids) * NUM_PERIODS:
                valid = False; break
        if not valid:
            continue

        # Apply
        for d in combo:
            class_day_count[cid][d] += 1
            if tid: teacher_day_count[tid][d] += 1
            if req_lab: lab_day_count[d] += 1
            day_schedule[d][cid].append((sid, tid, req_lab))

        if assign_days(idx + 1):
            return True

        # Revert
        for d in combo:
            class_day_count[cid][d] -= 1
            if tid: teacher_day_count[tid][d] -= 1
            if req_lab: lab_day_count[d] -= 1
            day_schedule[d][cid].pop()

    return False

stage1_success = assign_days(0)
print(f"Stage 1 (Days Distribution) Success: {stage1_success} in {round((time.time() - start_time)*1000, 2)}ms")

# STAGE 2: For each day d in 0..5, assign periods 0..7 and rooms
final_slots = []
stage2_success = True

for d in range(NUM_DAYS):
    # For day d, each class c has a list of items: day_schedule[d][c]
    # We need to map each item to period p in 0..7 and room r
    # Items to place on day d:
    day_items = []
    for cid, items in day_schedule[d].items():
        for sid, tid, req_lab in items:
            day_items.append({"class_id": cid, "subject_id": sid, "teacher_id": tid, "req_lab": req_lab})

    # Sort day items: lab items first
    day_items.sort(key=lambda x: x["req_lab"], reverse=True)

    class_period = defaultdict(dict) # class_id -> period -> item
    teacher_period = defaultdict(dict) # teacher_id -> period -> item
    room_period = defaultdict(dict) # room_id -> period -> item
    day_assignments = []

    def solve_day(idx):
        if idx == len(day_items):
            return True
        it = day_items[idx]
        cid = it["class_id"]
        sid = it["subject_id"]
        tid = it["teacher_id"]
        req_lab = it["req_lab"]

        # Periods candidate: 0..7
        candidate_periods = list(range(NUM_PERIODS))
        random.shuffle(candidate_periods)

        for p in candidate_periods:
            if p in class_period[cid]:
                continue
            if tid and p in teacher_period[tid]:
                continue

            # Pick room
            if req_lab:
                free_labs = [rid for rid in lab_room_ids if p not in room_period[rid]]
                if not free_labs:
                    continue
                chosen_room = free_labs[0]
            else:
                default_room = classes[cid].get("room_id")
                if default_room and p not in room_period[default_room]:
                    chosen_room = default_room
                else:
                    free_cr = [rid for rid in classroom_ids if p not in room_period[rid]]
                    chosen_room = free_cr[0] if free_cr else (classroom_ids[0] if classroom_ids else None)

            # Assign
            class_period[cid][p] = sid
            if tid: teacher_period[tid][p] = cid
            if chosen_room: room_period[chosen_room][p] = cid
            day_assignments.append({
                "class_id": cid, "day": d, "period": p,
                "subject_id": sid, "staff_id": tid, "room_id": chosen_room
            })

            if solve_day(idx + 1):
                return True

            # Backtrack
            del class_period[cid][p]
            if tid: del teacher_period[tid][p]
            if chosen_room: del room_period[chosen_room][p]
            day_assignments.pop()

        return False

    if not solve_day(0):
        print(f"Day {DAYS[d]} failed period assignment!")
        stage2_success = False
        break
    else:
        final_slots.extend(day_assignments)

total_time = round((time.time() - start_time) * 1000, 2)
print(f"Stage 2 (Period & Room Assignment) Success: {stage2_success}")
print(f"Total slots scheduled: {len(final_slots)} in {total_time}ms")

# Verify Clashes
clashes = 0
seen_class = set()
seen_staff = set()
seen_room = set()
for slot in final_slots:
    ck = (slot["class_id"], slot["day"], slot["period"])
    if ck in seen_class: print("CLASS CLASH", ck); clashes += 1
    seen_class.add(ck)

    if slot["staff_id"]:
        sk = (slot["staff_id"], slot["day"], slot["period"])
        if sk in seen_staff: print("STAFF CLASH", sk); clashes += 1
        seen_staff.add(sk)

    if slot["room_id"]:
        rk = (slot["room_id"], slot["day"], slot["period"])
        if rk in seen_room: print("ROOM CLASH", rk); clashes += 1
        seen_room.add(rk)

print(f"Total Clashes: {clashes}")
