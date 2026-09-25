# Smart Automatic Timetable Generator for Engineering Colleges 🚀

A full-stack institutional ERP web application designed to automatically generate conflict-free academic timetables for engineering colleges satisfying complex academic constraints, faculty workload limits, and continuous laboratory period rules using an AI-based **Genetic Algorithm (GA)** and **Constraint Satisfaction Problem (CSP)** engine.

---

## 🌟 Key Features

1. **AI-Powered 1-Click Timetable Generation:**
   - Genetic Algorithm optimization with population selection, uniform crossover, and period mutation.
   - Zero Hard Conflict Guarantee (No faculty double-booking, no classroom double-booking, no batch double-booking).
   - Continuous 3-period lab slot allocation.
   - Core subject morning period priority.

2. **HOD Drag-and-Drop Interactive Timetable Editor:**
   - Real-time AI conflict checker validating manual period slot swaps.

3. **Role-Based Access Control (RBAC):**
   - Super Admin, Admin, HOD, and Faculty user roles with JWT security.

4. **Faculty Leave & Automated Substitute Re-assignment:**
   - Faculty apply leave -> HOD approves -> System automatically assigns substitute faculty and updates active timetable entries.

5. **Analytics Dashboard & Reports:**
   - Recharts Faculty Workload Distribution and Classroom Utilization Donut charts.
   - CSV data report downloads for Faculty, Classroom Utilization, and Workload Audit.

---

## 🛠️ Technology Stack

* **Frontend:** React.js 18, Vite, Tailwind CSS, Material UI, React Router v6, Axios, Recharts, Lucide Icons
* **Backend:** Spring Boot 3.2, Java 21 LTS, Spring Security, JWT (JJWT), Maven, Spring Data JPA, Hibernate
* **Database:** MySQL 8.0

---

## 📁 Repository Structure

```
TT P/
├── backend/                  # Spring Boot 3 Java 21 Source Code
│   ├── src/main/java/com/smarttimetable/
│   │   ├── config/           # Security, JWT, CORS Configuration
│   │   ├── controller/       # REST API Endpoints
│   │   ├── dto/              # Request & Response Data Objects
│   │   ├── entity/           # JPA Entities
│   │   ├── enums/            # System Enums
│   │   ├── repository/       # Spring Data Repositories
│   │   ├── service/          # Business Logic & Services
│   │   └── service/scheduler/ # Smart AI Genetic Algorithm Engine
│   ├── mvnw.cmd              # Maven Wrapper
│   └── pom.xml               # Maven Dependencies
├── frontend/                 # Vite React 18 Frontend
│   ├── src/
│   │   ├── components/       # Navbar, Sidebar, DataGrid, DragDropGrid, Modal
│   │   ├── context/          # Auth & Theme Context
│   │   ├── pages/            # Dashboard, Generator, Timetables, Editor, CRUDs
│   │   └── services/         # Axios API Services
│   ├── package.json
│   └── vite.config.js
├── database/                 # MySQL Scripts
│   ├── schema.sql            # Table DDL & Indexes
│   └── data.sql              # College Seed Data
├── docs/                     # Documentation & Diagrams
│   ├── API_DOCUMENTATION.md
│   ├── ER_DIAGRAM.md
│   ├── USE_CASE_DIAGRAM.md
│   ├── CLASS_DIAGRAM.md
│   ├── SEQUENCE_DIAGRAM.md
│   └── DEPLOYMENT_GUIDE.md
└── README.md
```

---

## 🚀 Quick Start Guide

### 1. Database Setup
Execute the MySQL scripts in MySQL 8:
```sql
SOURCE database/schema.sql;
SOURCE database/data.sql;
```

### 2. Run Backend (Spring Boot 3)
```bash
cd backend
mvnw.cmd spring-boot:run
```
Backend runs on `http://localhost:8080` (Swagger UI: `http://localhost:8080/swagger-ui.html`).

### 3. Run Frontend (React Vite)
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:3000`.

---

## 🔑 Demo Login Credentials

* **Super Admin:** `superadmin` / `Password@123`
* **HOD (CSE):** `hod_cse` / `Password@123`
* **Faculty:** `prof_john` / `Password@123`
