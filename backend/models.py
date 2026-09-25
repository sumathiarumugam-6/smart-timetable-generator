from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class RoomBase(BaseModel):
    name: str
    room_type: str = "classroom"  # "classroom" or "lab"
    capacity: int = 60

class RoomCreate(RoomBase):
    pass

class Room(RoomBase):
    id: int

    class Config:
        from_attributes = True

class SubjectBase(BaseModel):
    code: str
    name: str
    weekly_hours: int = 4
    requires_lab: bool = False
    color: str = "#4f46e5"

class SubjectCreate(SubjectBase):
    pass

class Subject(SubjectBase):
    id: int

    class Config:
        from_attributes = True

class StaffBase(BaseModel):
    name: str
    email: Optional[str] = None
    subjects: List[int] = []  # List of subject IDs
    weekly_max_load: int = 20

class StaffCreate(StaffBase):
    pass

class Staff(StaffBase):
    id: int
    current_load: Optional[int] = 0

    class Config:
        from_attributes = True

class ClassSubjectAllocation(BaseModel):
    subject_id: int
    teacher_id: Optional[int] = None
    hours_per_week: int = 4

class ClassBase(BaseModel):
    name: str
    room_id: Optional[int] = None

class ClassCreate(ClassBase):
    allocations: List[ClassSubjectAllocation] = []

class ClassDetail(ClassBase):
    id: int
    room_name: Optional[str] = None
    allocations: List[Dict[str, Any]] = []
    total_hours: int = 0

class TimetableSlotResponse(BaseModel):
    id: Optional[int] = None
    class_id: int
    class_name: str
    day: int  # 0: Monday, 5: Saturday
    period: int  # 0: Period 1, 7: Period 8
    subject_id: int
    subject_name: str
    subject_code: str
    subject_color: str
    requires_lab: bool
    staff_id: int
    staff_name: str
    room_id: Optional[int] = None
    room_name: Optional[str] = None

class ConflictDetail(BaseModel):
    type: str  # "hours_exceeded", "teacher_overloaded", "no_teacher", "no_lab", "unsolvable_clash"
    title: str
    description: str
    entity_id: Optional[int] = None
    entity_name: Optional[str] = None

class TimetableGenerationResult(BaseModel):
    success: bool
    message: str
    total_slots_scheduled: int = 0
    execution_time_ms: float = 0.0
    conflicts: List[ConflictDetail] = []
    timetable: List[TimetableSlotResponse] = []
