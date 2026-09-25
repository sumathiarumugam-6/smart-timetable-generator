import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Cpu,
  Calendar,
  Move,
  Building2,
  Users,
  BookOpen,
  DoorOpen,
  GraduationCap,
  CalendarOff,
  FileSpreadsheet,
} from 'lucide-react';

export const Sidebar = () => {
  const { hasRole } = useAuth();

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY'] },
    { label: 'AI Timetable Generator', path: '/generator', icon: Cpu, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD'] },
    { label: 'View Timetables', path: '/timetables', icon: Calendar, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY'] },
    { label: 'Drag & Drop Editor', path: '/editor', icon: Move, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD'] },
    { label: 'Departments', path: '/departments', icon: Building2, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN'] },
    { label: 'Faculty Members', path: '/faculties', icon: Users, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD'] },
    { label: 'Subjects', path: '/subjects', icon: BookOpen, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD'] },
    { label: 'Classrooms & Labs', path: '/classrooms', icon: DoorOpen, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN'] },
    { label: 'Batches', path: '/batches', icon: GraduationCap, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD'] },
    { label: 'Leave Management', path: '/leaves', icon: CalendarOff, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY'] },
    { label: 'Reports & Exports', path: '/reports', icon: FileSpreadsheet, roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD', 'ROLE_FACULTY'] },
  ];

  return (
    <aside className="w-64 glass-panel border-r border-slate-800 bg-slate-900/90 flex flex-col shrink-0 min-h-[calc(100vh-61px)]">
      <div className="p-4 space-y-1">
        <p className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-2">Navigation Menu</p>
        {navItems.map((item) => {
          if (!hasRole(...item.roles)) return null;
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-gradient-to-r from-indigo-600 to-indigo-700 text-white shadow-lg shadow-indigo-600/30 font-semibold'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="h-4 w-4" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </div>
    </aside>
  );
};
