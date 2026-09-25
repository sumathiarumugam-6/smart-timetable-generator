import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';

import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { TimetableGenerator } from './pages/TimetableGenerator';
import { Timetables } from './pages/Timetables';
import { DragDropEditorPage } from './pages/DragDropEditorPage';
import { DepartmentsPage } from './pages/DepartmentsPage';
import { FacultyPage } from './pages/FacultyPage';
import { SubjectsPage } from './pages/SubjectsPage';
import { ClassroomsPage } from './pages/ClassroomsPage';
import { BatchesPage } from './pages/BatchesPage';
import { LeaveManagementPage } from './pages/LeaveManagementPage';
import { ReportsPage } from './pages/ReportsPage';

export const App = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<Layout />}>
                <Route path="/" element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/generator" element={<TimetableGenerator />} />
                <Route path="/timetables" element={<Timetables />} />
                <Route path="/editor" element={<DragDropEditorPage />} />
                <Route path="/departments" element={<DepartmentsPage />} />
                <Route path="/faculties" element={<FacultyPage />} />
                <Route path="/subjects" element={<SubjectsPage />} />
                <Route path="/classrooms" element={<ClassroomsPage />} />
                <Route path="/batches" element={<BatchesPage />} />
                <Route path="/leaves" element={<LeaveManagementPage />} />
                <Route path="/reports" element={<ReportsPage />} />
              </Route>
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
