import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { timetableService } from '../services/timetableService';
import { departmentService } from '../services/departmentService';
import { Cpu, Sparkles, CheckCircle2, ArrowRight, AlertCircle } from 'lucide-react';

export const TimetableGenerator = () => {
  const [departments, setDepartments] = useState([]);
  const [departmentId, setDepartmentId] = useState('');
  const [semester, setSemester] = useState(5);
  const [academicYear, setAcademicYear] = useState('2025-2026');
  const [title, setTitle] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    departmentService.getAll().then((depts) => {
      setDepartments(depts);
      if (depts.length > 0) {
        setDepartmentId(depts[0].id);
        setTitle(`${depts[0].code} Semester ${semester} Timetable (${academicYear})`);
      }
    });
  }, []);

  const handleDepartmentChange = (e) => {
    const dId = e.target.value;
    setDepartmentId(dId);
    const d = departments.find((dept) => dept.id.toString() === dId);
    if (d) {
      setTitle(`${d.code} Semester ${semester} Timetable (${academicYear})`);
    }
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setResult(null);

    try {
      const generated = await timetableService.generate({
        title,
        academicYear,
        departmentId: parseInt(departmentId),
        semester: parseInt(semester),
      });

      setResult(generated);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate timetable.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="glass-card rounded-3xl p-8 border border-slate-800 space-y-6">
        <div className="flex items-center space-x-4">
          <div className="h-14 w-14 rounded-2xl bg-gradient-to-tr from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center text-white shadow-xl shadow-indigo-500/30">
            <Cpu className="h-7 w-7" />
          </div>
          <div>
            <h1 className="text-2xl font-extrabold text-white">Smart AI Timetable Generator</h1>
            <p className="text-xs text-slate-400">
              Genetic Algorithm & Constraint Satisfaction Engine (1-Click Conflict-Free Generation)
            </p>
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-300 flex items-center space-x-2">
            <AlertCircle className="h-4 w-4 text-red-400 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleGenerate} className="grid grid-cols-1 md:grid-cols-2 gap-5 pt-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Select Department
            </label>
            <select
              value={departmentId}
              onChange={handleDepartmentChange}
              className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition"
            >
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.code})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Academic Semester
            </label>
            <select
              value={semester}
              onChange={(e) => setSemester(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition"
            >
              {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                <option key={s} value={s}>
                  Semester {s}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Academic Year
            </label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Timetable Title
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-900 border border-slate-700/80 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 transition"
            />
          </div>

          <div className="md:col-span-2 pt-4">
            <button
              type="submit"
              disabled={loading}
              className="w-full py-4 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 hover:from-indigo-500 hover:to-pink-500 text-white font-bold text-sm rounded-xl transition shadow-xl shadow-indigo-600/30 flex items-center justify-center space-x-2"
            >
              {loading ? (
                <div className="flex items-center space-x-2">
                  <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-white"></div>
                  <span>Running AI Genetic Algorithm (Generations 1-300)...</span>
                </div>
              ) : (
                <>
                  <Sparkles className="h-5 w-5" />
                  <span>Generate Conflict-Free Timetable (1-Click)</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {result && (
        <div className="glass-card rounded-3xl p-6 border border-emerald-500/30 bg-emerald-950/20 space-y-4 animate-fade-in">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="h-10 w-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
                <CheckCircle2 className="h-6 w-6" />
              </div>
              <div>
                <h3 className="font-bold text-lg text-emerald-200">Generation Complete!</h3>
                <p className="text-xs text-slate-400">{result.title}</p>
              </div>
            </div>

            <button
              onClick={() => navigate(`/timetables`)}
              className="flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl transition shadow-lg"
            >
              <span>View & Publish Timetable</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>

          <div className="grid grid-cols-3 gap-4 pt-2 text-center">
            <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Fitness Score</p>
              <p className="text-xl font-extrabold text-indigo-400">{result.fitnessScore?.toFixed(1)} / 100</p>
            </div>
            <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Hard Conflicts</p>
              <p className="text-xl font-extrabold text-emerald-400">{result.conflictCount} Conflicts</p>
            </div>
            <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
              <p className="text-[10px] text-slate-400 uppercase font-semibold">Scheduled Entries</p>
              <p className="text-xl font-extrabold text-purple-400">{result.entries?.length || 0} Slots</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
