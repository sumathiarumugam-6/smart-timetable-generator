# Smart Timetable Generator - Deployment Guide

## Prerequisites
- Java JDK 21 LTS
- Node.js (v18+ / v20+ / v24+)
- MySQL Server 8.0+

---

## 1. Database Setup (MySQL 8)

1. Connect to MySQL server:
```bash
mysql -u root -p
```
2. Execute the schema and seed scripts:
```sql
SOURCE database/schema.sql;
SOURCE database/data.sql;
```

---

## 2. Backend Setup & Execution (Spring Boot 3)

1. Navigate to backend directory:
```bash
cd backend
```
2. Build executable JAR using Maven wrapper:
```bash
mvnw.cmd clean package -DskipTests
```
3. Run Spring Boot application:
```bash
java -jar target/smart-timetable-generator-1.0.0.jar
```
* Or run using Spring Boot plugin:
```bash
mvnw.cmd spring-boot:run
```
* Application will start on port `8080`.
* Swagger API Documentation available at: `http://localhost:8080/swagger-ui.html`

---

## 3. Frontend Setup & Execution (Vite + React)

1. Navigate to frontend directory:
```bash
cd frontend
```
2. Install Node dependencies:
```bash
npm install
```
3. Start development server:
```bash
npm run dev
```
* Application UI available at: `http://localhost:3000`

---

## 4. Default Login Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Super Admin** | `superadmin` | `Password@123` |
| **HOD (CSE)** | `hod_cse` | `Password@123` |
| **HOD (ECE)** | `hod_ece` | `Password@123` |
| **Faculty Member** | `prof_john` | `Password@123` |
