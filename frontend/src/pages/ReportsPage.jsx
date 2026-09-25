import React, { useEffect, useState } from 'react';
import { reportService } from '../services/reportService';
import { facultyService } from '../services/facultyService';
import { FileSpreadsheet, Download, Printer, Users, DoorOpen, Award } from 'lucide-react';

export const ReportsPage = () => {
  const [faculties, setFaculties] = useState([]);
  const [selectedFacultyId, setSelectedFacultyId] = useState('');

  useEffect(() => {
    facultyService.getAll().then((data) => {
      setFaculties(data);
      if (data.length > 0) setSelectedFacultyId(data[0].id);
    });
  }, []);

  const downloadFile = (blob, filename) => {
    const url = window.URL.createObjectURL(new Blob([blob]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
  };

  const handleExportFaculty = async () => {
    if (!selectedFacultyId) return;
    try {
      const blob = await reportService.exportFacultyReport(selectedFacultyId);
      downloadFile(blob, `faculty_${selectedFacultyId}_timetable_report.csv`);
    } catch (err) {
      alert('Failed to download faculty report');
    }
  };

  const handleExportClassrooms = async () => {
    try {
      const blob = await reportService.exportClassroomReport();
      downloadFile(blob, `classroom_utilization_report.csv`);
    } catch (err) {
      alert('Failed to download classroom report');
    }
  };

  const handleExportWorkload = async () => {
    try {
      const blob = await reportService.exportWorkloadReport();
      downloadFile(blob, `faculty_workload_report.csv`);
    } catch (err) {
      alert('Failed to download workload report');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-extrabold text-white">Reports & Data Exports</h1>
        <p className="text-xs text-slate-400">Generate and download institutional timetables, room utilization, and workload reports in Excel CSV format.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Faculty-wise Report Card */}
        <div className="glass-card rounded-2xl p-6 border border-slate-800 space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="h-11 w-11 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
              <Users className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-lg text-white">Faculty-Wise Timetable</h3>
            <p className="text-xs text-slate-400">Export individual teacher schedules showing days, period slots, and assigned classrooms.</p>

            <select
              value={selectedFacultyId}
              onChange={(e) => setSelectedFacultyId(e.target.value)}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-200 mt-2"
            >
              {faculties.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name} ({f.facultyCode})
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={handleExportFaculty}
            className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl transition shadow-lg flex items-center justify-center space-x-2"
          >
            <Download className="h-4 w-4" />
            <span>Export CSV Report</span>
          </button>
        </div>

        {/* Classroom Utilization Card */}
        <div className="glass-card rounded-2xl p-6 border border-slate-800 space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="h-11 w-11 rounded-xl bg-purple-500/20 text-purple-400 flex items-center justify-center">
              <DoorOpen className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-lg text-white">Classroom & Lab Utilization</h3>
            <p className="text-xs text-slate-400">Comprehensive room occupancy metrics, capacity usage, and weekly booked period count.</p>
          </div>

          <button
            onClick={handleExportClassrooms}
            className="w-full py-2.5 bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold rounded-xl transition shadow-lg flex items-center justify-center space-x-2"
          >
            <Download className="h-4 w-4" />
            <span>Export CSV Report</span>
          </button>
        </div>

        {/* Faculty Workload Card */}
        <div className="glass-card rounded-2xl p-6 border border-slate-800 space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="h-11 w-11 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
              <Award className="h-6 w-6" />
            </div>
            <h3 className="font-bold text-lg text-white">Faculty Workload Audit</h3>
            <p className="text-xs text-slate-400">Institutional teaching hours audit report for tracking overload and free hours compliance.</p>
          </div>

          <button
            onClick={handleExportWorkload}
            className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl transition shadow-lg flex items-center justify-center space-x-2"
          >
            <Download className="h-4 w-4" />
            <span>Export CSV Report</span>
          </button>
        </div>
      </div>
    </div>
  );
};
