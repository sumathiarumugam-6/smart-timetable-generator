# Smart Automatic Timetable Generator - REST API Documentation

## Overview
Base URL: `http://localhost:8080/api`  
Authentication: `Bearer <JWT_TOKEN>` in HTTP `Authorization` Header  
Content-Type: `application/json`

---

## 1. Authentication APIs (`/api/auth`)

### 1.1 Login User
* **Endpoint:** `POST /api/auth/login`
* **Request Body:**
```json
{
  "username": "superadmin",
  "password": "Password@123"
}
```
* **Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "superadmin",
  "email": "admin@enggcollege.edu",
  "fullName": "System Super Admin",
  "role": "ROLE_SUPER_ADMIN",
  "departmentId": 1,
  "departmentName": "Computer Science & Engineering"
}
```

### 1.2 Register User
* **Endpoint:** `POST /api/auth/register`
* **Request Body:**
```json
{
  "username": "prof_sarah",
  "email": "sarah@enggcollege.edu",
  "password": "Password@123",
  "fullName": "Dr. Sarah Vance",
  "role": "ROLE_FACULTY",
  "departmentId": 1
}
```

---

## 2. Timetable Management APIs (`/api/timetables`)

### 2.1 Generate Timetable (AI Genetic Algorithm Trigger)
* **Endpoint:** `POST /api/timetables/generate`
* **Security:** `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_HOD`
* **Request Body:**
```json
{
  "title": "CSE Semester 5 Master Timetable",
  "academicYear": "2025-2026",
  "departmentId": 1,
  "semester": 5
}
```

### 2.2 Manual Drag-and-Drop Swap
* **Endpoint:** `POST /api/timetables/manual-swap`
* **Security:** `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_HOD`
* **Request Body:**
```json
{
  "timetableId": 1,
  "entryId1": 15,
  "targetDay": "WEDNESDAY",
  "targetPeriodNumber": 4,
  "targetClassroomId": 2
}
```

---

## 3. Leave & Substitution APIs (`/api/leaves`)

### 3.1 Apply Leave
* **Endpoint:** `POST /api/leaves/apply`
```json
{
  "facultyId": 2,
  "startDate": "2025-10-15",
  "endDate": "2025-10-16",
  "reason": "Attending International AI Symposium"
}
```

### 3.2 Approve Leave & Auto-Assign Substitute
* **Endpoint:** `PUT /api/leaves/{id}/approve`
* **Security:** `ROLE_HOD`, `ROLE_ADMIN`

---

## 4. Reports & Exports (`/api/reports`)

* `GET /api/reports/faculty/{facultyId}/export` - Download Faculty Timetable CSV
* `GET /api/reports/classrooms/export` - Download Classroom Occupancy CSV
* `GET /api/reports/workload/export` - Download Faculty Workload Audit CSV
