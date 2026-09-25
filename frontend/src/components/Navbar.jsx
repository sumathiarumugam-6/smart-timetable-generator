import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { Sun, Moon, Bell, LogOut, User, Sparkles } from 'lucide-react';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [showNotifs, setShowNotifs] = useState(false);

  const notifications = [
    { id: 1, title: 'Timetable Generated', text: 'CSE Sem 5 timetable generated with 0 conflicts.', time: '10m ago', type: 'success' },
    { id: 2, title: 'Leave Application', text: 'Prof. Alice Smith applied for leave on Oct 15.', time: '1h ago', type: 'info' }
  ];

  return (
    <header className="sticky top-0 z-40 w-full glass-panel border-b border-slate-800 bg-slate-900/80">
      <div className="px-6 py-3 flex items-center justify-between">
        {/* Left Branding */}
        <div className="flex items-center space-x-3">
          <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
            <Sparkles className="h-5 w-5 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold bg-gradient-to-r from-white via-slate-200 to-indigo-300 bg-clip-text text-transparent">
              Smart Timetable Generator
            </h1>
            <p className="text-xs text-slate-400 font-medium">Engineering College ERP System</p>
          </div>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-4">
          {/* Theme Toggle */}
          <button
            onClick={toggleTheme}
            className="p-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-all border border-slate-800"
            title="Toggle Light/Dark Theme"
          >
            {theme === 'dark' ? <Sun className="h-5 w-5 text-amber-400" /> : <Moon className="h-5 w-5 text-indigo-400" />}
          </button>

          {/* Notifications */}
          <div className="relative">
            <button
              onClick={() => setShowNotifs(!showNotifs)}
              className="p-2.5 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-all relative border border-slate-800"
            >
              <Bell className="h-5 w-5" />
              <span className="absolute top-2 right-2 h-2 w-2 rounded-full bg-indigo-500 ring-4 ring-slate-900 animate-pulse"></span>
            </button>

            {showNotifs && (
              <div className="absolute right-0 mt-3 w-80 rounded-2xl glass-card border border-slate-800 p-4 shadow-2xl z-50">
                <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                  <h3 className="font-semibold text-sm text-slate-200">Notifications</h3>
                  <span className="text-xs bg-indigo-500/20 text-indigo-400 px-2 py-0.5 rounded-full font-medium">2 New</span>
                </div>
                <div className="mt-3 space-y-2">
                  {notifications.map((n) => (
                    <div key={n.id} className="p-2.5 rounded-xl bg-slate-800/50 hover:bg-slate-800 transition border border-slate-700/40">
                      <p className="text-xs font-semibold text-indigo-300">{n.title}</p>
                      <p className="text-xs text-slate-300 mt-1">{n.text}</p>
                      <span className="text-[10px] text-slate-500 block mt-1">{n.time}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* User Profile */}
          <div className="flex items-center space-x-3 pl-4 border-l border-slate-800">
            <div className="h-9 w-9 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-indigo-400 font-bold text-sm">
              {user?.fullName ? user.fullName.charAt(0) : 'U'}
            </div>
            <div className="hidden md:block">
              <p className="text-sm font-semibold text-slate-200">{user?.fullName || 'Guest User'}</p>
              <span className="inline-block text-[10px] font-bold px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                {user?.role?.replace('ROLE_', '') || 'FACULTY'}
              </span>
            </div>

            <button
              onClick={logout}
              className="p-2 rounded-xl text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition"
              title="Logout"
            >
              <LogOut className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
