import React, { useEffect, useState } from 'react';
import { dashboardService } from '../services/dashboardService';
import {
  Building2,
  Users,
  BookOpen,
  DoorOpen,
  CalendarCheck,
  TrendingUp,
  Cpu,
  Sparkles,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from 'recharts';

export const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await dashboardService.getStats();
      setStats(data);
    } catch (err) {
      console.error('Failed to load dashboard stats', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 text-center text-slate-400">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-indigo-500 mx-auto"></div>
        <p className="mt-4 text-xs font-semibold">Loading ERP Analytics...</p>
      </div>
    );
  }

  const metricCards = [
    { title: 'Total Departments', value: stats?.totalDepartments || 5, icon: Building2, color: 'from-blue-500 to-indigo-600' },
    { title: 'Total Faculty', value: stats?.totalFaculty || 12, icon: Users, color: 'from-purple-500 to-pink-600' },
    { title: 'Total Subjects', value: stats?.totalSubjects || 24, icon: BookOpen, color: 'from-emerald-500 to-teal-600' },
    { title: 'Classrooms & Labs', value: (stats?.totalClassrooms || 8) + (stats?.totalLabs || 4), icon: DoorOpen, color: 'from-amber-500 to-orange-600' },
    { title: 'Generated Timetables', value: stats?.generatedTimetables || 3, icon: CalendarCheck, color: 'from-cyan-500 to-blue-600' },
  ];

  const COLORS = ['#6366f1', '#a855f7', '#10b981', '#f59e0b', '#ec4899'];

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="glass-card rounded-3xl p-6 border border-slate-800 bg-gradient-to-r from-slate-900 via-indigo-950/40 to-slate-900 relative overflow-hidden">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 relative z-10">
          <div>
            <div className="flex items-center space-x-2 text-indigo-400 text-xs font-bold uppercase tracking-wider mb-1">
              <Sparkles className="h-4 w-4" />
              <span>Smart Genetic Algorithm Engine Ready</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">Institutional Timetable Analytics</h1>
            <p className="text-xs text-slate-400 mt-1">Real-time faculty workload, classroom occupancy, and conflict statistics.</p>
          </div>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
        {metricCards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div key={idx} className="glass-card rounded-2xl p-4 border border-slate-800 flex items-center justify-between">
              <div>
                <p className="text-xs font-medium text-slate-400">{card.title}</p>
                <p className="text-2xl font-extrabold text-white mt-1">{card.value}</p>
              </div>
              <div className={`h-11 w-11 rounded-xl bg-gradient-to-tr ${card.color} flex items-center justify-center text-white shadow-lg`}>
                <Icon className="h-5 w-5" />
              </div>
            </div>
          );
        })}
      </div>

      {/* Recharts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Faculty Workload Chart */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div>
              <h3 className="font-bold text-sm text-slate-200">Faculty Workload Distribution</h3>
              <p className="text-xs text-slate-400">Assigned periods vs maximum weekly capacity</p>
            </div>
            <TrendingUp className="h-5 w-5 text-indigo-400" />
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={stats?.facultyWorkloadStats || []}>
                <XAxis dataKey="facultyCode" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={11} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }} />
                <Bar dataKey="assignedPeriods" name="Assigned Periods" fill="#6366f1" radius={[6, 6, 0, 0]} />
                <Bar dataKey="maxHours" name="Max Limit" fill="#334155" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Room Utilization Donut Chart */}
        <div className="glass-card rounded-2xl p-5 border border-slate-800 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div>
              <h3 className="font-bold text-sm text-slate-200">Classroom Utilization %</h3>
              <p className="text-xs text-slate-400">Weekly occupancy percentage by room</p>
            </div>
            <DoorOpen className="h-5 w-5 text-purple-400" />
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={stats?.classroomUtilizationStats || []}
                  dataKey="utilizationPercentage"
                  nameKey="roomNumber"
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={5}
                  label
                >
                  {(stats?.classroomUtilizationStats || []).map((_, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
