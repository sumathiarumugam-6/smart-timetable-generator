import React, { useEffect, useState } from 'react';
import { timetableService } from '../services/timetableService';
import { departmentService } from '../services/departmentService';
import { facultyService } from '../services/facultyService';
import { classroomService } from '../services/classroomService';
import { DragDropTimetableGrid } from '../components/DragDropTimetableGrid';
import { Modal } from '../components/Modal';
import {
  Calendar,
  Printer,
  FileSpreadsheet,
  FileText,
  Search,
  Filter,
  CheckCircle2,
  AlertTriangle,
  Building2,
  Users,
  DoorOpen,
  Plus,
  Sparkles,
  ArrowRight,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Timetables = () => {
  const [timetables, setTimetables] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [classrooms, setClassrooms] = useState([]);

  // Selected Timetable & Filter States
  const [selectedTimetable, setSelectedTimetable] = useState(null);
  const [selectedDeptId, setSelectedDeptId] = useState('');
  const [semFilter, setSemFilter] = useState('');
  const [sectionFilter, setSectionFilter] = useState('ALL');
  const [facultyFilter, setFacultyFilter] = useState('ALL');
  const [roomFilter, setRoomFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  // Quick Add Timetable Modal State
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newTtData, setNewTtData] = useState({
    title: '',
    academicYear: '2025-2026',
    departmentId: '',
    semester: 5,
  });
  const [generating, setGenerating] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    try {
      const [ttData, deptData, facData, roomData] = await Promise.all([
        timetableService.getAll(),
        departmentService.getAll(),
        facultyService.getAll(),
        classroomService.getAll(),
      ]);

      setTimetables(ttData);
      setDepartments(deptData);
      setFaculties(facData);
      setClassrooms(roomData);

      if (deptData.length > 0) {
        const firstDept = deptData[0];
        setSelectedDeptId(firstDept.id.toString());
        
        // Find matching timetable for first department
        const matchingTt = ttData.find((t) => t.departmentId.toString() === firstDept.id.toString());
        setSelectedTimetable(matchingTt || ttData[0] || null);

        setNewTtData({
          title: `${firstDept.code} Semester 5 Master Timetable (2025-2026)`,
          academicYear: '2025-2026',
          departmentId: firstDept.id.toString(),
          semester: 5,
        });
      }
    } catch (err) {
      console.error('Failed to load timetables data', err);
    } finally {
      setLoading(false);
    }
  };

  // Handle Department Change -> Auto select department's timetable
  const handleDepartmentSelect = (deptIdStr) => {
    setSelectedDeptId(deptIdStr);

    if (!deptIdStr) {
      if (timetables.length > 0) setSelectedTimetable(timetables[0]);
      return;
    }

    // Find timetable belonging to selected department
    const matchingTt = timetables.find((t) => t.departmentId.toString() === deptIdStr);
    if (matchingTt) {
      setSelectedTimetable(matchingTt);
    } else {
      setSelectedTimetable(null); // Department has no timetable yet
    }
  };

  // Handle Direct Timetable Selector Dropdown Change
  const handleTimetableSelect = (ttIdStr) => {
    const tt = timetables.find((t) => t.id.toString() === ttIdStr);
    if (tt) {
      setSelectedTimetable(tt);
      setSelectedDeptId(tt.departmentId.toString());
    }
  };

  // Handle Quick Add / Generate Timetable submit
  const handleCreateTimetableSubmit = async (e) => {
    e.preventDefault();
    setGenerating(true);
    try {
      const generated = await timetableService.generate({
        title: newTtData.title,
        academicYear: newTtData.academicYear,
        departmentId: parseInt(newTtData.departmentId),
        semester: parseInt(newTtData.semester),
      });

      setIsAddModalOpen(false);
      await loadAllData();
      setSelectedDeptId(generated.departmentId.toString());
      setSelectedTimetable(generated);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to generate timetable.');
    } finally {
      setGenerating(false);
    }
  };

  // Filter timetable entries based on active filters
  const getFilteredEntries = () => {
    if (!selectedTimetable || !selectedTimetable.entries) return [];

    return selectedTimetable.entries.filter((entry) => {
      // Semester filter
      if (semFilter && selectedTimetable.semester.toString() !== semFilter) return false;

      // Section filter
      if (sectionFilter !== 'ALL' && entry.batchSection !== sectionFilter) return false;

      // Faculty filter
      if (facultyFilter !== 'ALL' && entry.facultyId.toString() !== facultyFilter) return false;

      // Classroom filter
      if (roomFilter !== 'ALL' && entry.classroomId.toString() !== roomFilter) return false;

      // Search query
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        const matches =
          (entry.subjectName && entry.subjectName.toLowerCase().includes(q)) ||
          (entry.subjectCode && entry.subjectCode.toLowerCase().includes(q)) ||
          (entry.facultyName && entry.facultyName.toLowerCase().includes(q)) ||
          (entry.roomNumber && entry.roomNumber.toLowerCase().includes(q));
        if (!matches) return false;
      }

      return true;
    });
  };

  const filteredEntries = getFilteredEntries();

  // Export Timetable Grid to Excel CSV
  const handleExportExcel = () => {
    if (!selectedTimetable) return;

    let csvContent = `data:text/csv;charset=utf-8,`;
    csvContent += `MASTER TIMETABLE REPORT - ${selectedTimetable.title.toUpperCase()}\n`;
    csvContent += `Department: ${selectedTimetable.departmentName}, Semester: ${selectedTimetable.semester}, Academic Year: ${selectedTimetable.academicYear}\n\n`;

    csvContent += `Day,Period,Time Slot,Subject Code,Subject Name,Type,Faculty Name,Classroom,Section\n`;

    filteredEntries.forEach((e) => {
      csvContent += `"${e.dayOfWeek}",${e.periodNumber},"${e.timeSlot}","${e.subjectCode}","${e.subjectName}","${e.isLabSession ? 'LAB' : 'THEORY'}","${e.facultyName}","${e.roomNumber}","${e.batchSection}"\n`;
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${selectedTimetable.title.replace(/\s+/g, '_')}_timetable.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <div className="py-20 text-center text-slate-400">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-indigo-500 mx-auto"></div>
        <p className="mt-4 text-xs font-semibold">Loading Department Timetables...</p>
      </div>
    );
  }

  const currentDeptObj = departments.find((d) => d.id.toString() === selectedDeptId);

  return (
    <div className="space-y-6">
      {/* Module Header */}
      <div className="glass-card rounded-3xl p-6 border border-slate-800 bg-gradient-to-r from-slate-900 via-indigo-950/40 to-slate-900 flex flex-col md:flex-row md:items-center justify-between gap-4 print:hidden">
        <div>
          <div className="flex items-center space-x-2 text-indigo-400 text-xs font-bold uppercase tracking-wider mb-1">
            <Sparkles className="h-4 w-4" />
            <span>Department Timetable Viewer & Add Engine</span>
          </div>
          <h1 className="text-2xl font-extrabold text-white">View Department Timetables</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Select any department to view its master timetable, add new timetables, or export reports.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-bold rounded-xl transition shadow-lg shadow-indigo-600/30"
          >
            <Plus className="h-4 w-4" />
            <span>Add / Generate Timetable</span>
          </button>

          <button
            onClick={handlePrint}
            className="flex items-center space-x-2 px-3.5 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold rounded-xl transition border border-slate-700"
          >
            <Printer className="h-4 w-4 text-indigo-400" />
            <span>Print</span>
          </button>

          <button
            onClick={handleExportExcel}
            className="flex items-center space-x-2 px-3.5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl transition shadow-lg shadow-emerald-600/30"
          >
            <FileSpreadsheet className="h-4 w-4" />
            <span>Export Excel</span>
          </button>
        </div>
      </div>

      {/* Department Quick Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-none print:hidden">
        {departments.map((dept) => {
          const isSelected = selectedDeptId === dept.id.toString();
          const hasTt = timetables.some((t) => t.departmentId.toString() === dept.id.toString());

          return (
            <button
              key={dept.id}
              onClick={() => handleDepartmentSelect(dept.id.toString())}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition flex items-center space-x-2 shrink-0 border ${
                isSelected
                  ? 'bg-indigo-600 text-white border-indigo-500 shadow-lg shadow-indigo-600/30'
                  : 'bg-slate-900/80 text-slate-400 hover:text-slate-200 border-slate-800 hover:bg-slate-800'
              }`}
            >
              <Building2 className="h-4 w-4" />
              <span>{dept.name} ({dept.code})</span>
              {hasTt && (
                <span className={`h-2 w-2 rounded-full ${isSelected ? 'bg-white' : 'bg-emerald-400'}`}></span>
              )}
            </button>
          );
        })}
      </div>

      {/* Filter Toolbar */}
      <div className="glass-card rounded-2xl p-5 border border-slate-800 space-y-4 print:hidden">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2 text-xs font-bold text-slate-300 uppercase tracking-wider">
            <Filter className="h-4 w-4 text-indigo-400" />
            <span>Filter Timetable Details</span>
          </div>

          {currentDeptObj && (
            <span className="text-xs font-semibold text-indigo-300 bg-indigo-500/10 px-3 py-1 rounded-full border border-indigo-500/20">
              Department: {currentDeptObj.name} ({currentDeptObj.code})
            </span>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-3">
          {/* Select Timetable Dropdown for Current Dept */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Select Timetable Version</label>
            <select
              value={selectedTimetable?.id || ''}
              onChange={(e) => handleTimetableSelect(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-100 font-medium"
            >
              {timetables
                .filter((t) => !selectedDeptId || t.departmentId.toString() === selectedDeptId)
                .map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.title}
                  </option>
                ))}
              {timetables.filter((t) => !selectedDeptId || t.departmentId.toString() === selectedDeptId).length === 0 && (
                <option value="">No Timetable for this Dept</option>
              )}
            </select>
          </div>

          {/* Section Filter */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Section</label>
            <select
              value={sectionFilter}
              onChange={(e) => setSectionFilter(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-100 font-medium"
            >
              <option value="ALL">All Sections</option>
              <option value="A">Section A</option>
              <option value="B">Section B</option>
            </select>
          </div>

          {/* Faculty Filter */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Faculty Member</label>
            <select
              value={facultyFilter}
              onChange={(e) => setFacultyFilter(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-100 font-medium"
            >
              <option value="ALL">All Faculty</option>
              {faculties.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </div>

          {/* Classroom Filter */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Classroom / Lab</label>
            <select
              value={roomFilter}
              onChange={(e) => setRoomFilter(e.target.value)}
              className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-100 font-medium"
            >
              <option value="ALL">All Rooms</option>
              {classrooms.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.roomNumber} ({r.roomType})
                </option>
              ))}
            </select>
          </div>

          {/* Live Search Input */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Search Keywords</label>
            <div className="relative">
              <Search className="absolute left-3 top-2.5 h-3.5 w-3.5 text-slate-400" />
              <input
                type="text"
                placeholder="Search subject, faculty, room..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Timetable Header Summary */}
      {selectedTimetable && (
        <div className="glass-card rounded-2xl p-4 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs">
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="font-bold text-white text-base">{selectedTimetable.title}</h2>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 uppercase">
                {selectedTimetable.status}
              </span>
            </div>
            <p className="text-slate-400 mt-0.5">
              Department: {selectedTimetable.departmentName} | Semester: {selectedTimetable.semester} | Academic Year: {selectedTimetable.academicYear}
            </p>
          </div>

          <div className="flex items-center space-x-4">
            <div className="text-right">
              <p className="text-[10px] text-slate-400 font-semibold uppercase">AI Fitness Score</p>
              <p className="font-extrabold text-indigo-400 text-sm">{selectedTimetable.fitnessScore?.toFixed(1)} / 100</p>
            </div>
            <div className="text-right pl-4 border-l border-slate-800">
              <p className="text-[10px] text-slate-400 font-semibold uppercase">Scheduled Classes</p>
              <p className="font-extrabold text-purple-400 text-sm">{filteredEntries.length} Slots</p>
            </div>
          </div>
        </div>
      )}

      {/* Timetable Grid View OR Empty Department Prompt */}
      {selectedTimetable ? (
        <DragDropTimetableGrid
          entries={filteredEntries}
          editable={false}
          searchFilter={searchQuery}
        />
      ) : (
        <div className="glass-card rounded-3xl p-12 text-center text-slate-400 space-y-4 border border-slate-800">
          <div className="h-16 w-16 rounded-2xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mx-auto">
            <Building2 className="h-8 w-8" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-white">No Timetable for {currentDeptObj ? currentDeptObj.name : 'this Department'}</h3>
            <p className="text-xs text-slate-400 mt-1">
              Click the button below to generate a conflict-free master timetable for {currentDeptObj ? currentDeptObj.code : 'this department'}.
            </p>
          </div>

          <button
            onClick={() => {
              if (currentDeptObj) {
                setNewTtData({
                  title: `${currentDeptObj.code} Semester 5 Master Timetable (2025-2026)`,
                  academicYear: '2025-2026',
                  departmentId: currentDeptObj.id.toString(),
                  semester: 5,
                });
              }
              setIsAddModalOpen(true);
            }}
            className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs rounded-xl shadow-lg inline-flex items-center space-x-2 transition"
          >
            <Sparkles className="h-4 w-4" />
            <span>Generate Timetable for {currentDeptObj ? currentDeptObj.code : 'Department'}</span>
          </button>
        </div>
      )}

      {/* Add / Generate Timetable Modal */}
      <Modal isOpen={isAddModalOpen} onClose={() => setIsAddModalOpen(false)} title="Add / Generate New Department Timetable">
        <form onSubmit={handleCreateTimetableSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Select Department</label>
            <select
              value={newTtData.departmentId}
              onChange={(e) => {
                const dId = e.target.value;
                const d = departments.find((dept) => dept.id.toString() === dId);
                setNewTtData({
                  ...newTtData,
                  departmentId: dId,
                  title: d ? `${d.code} Semester ${newTtData.semester} Master Timetable (2025-2026)` : newTtData.title,
                });
              }}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            >
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.code})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Academic Semester</label>
              <select
                value={newTtData.semester}
                onChange={(e) => setNewTtData({ ...newTtData, semester: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                  <option key={s} value={s}>
                    Semester {s}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Academic Year</label>
              <input
                type="text"
                required
                value={newTtData.academicYear}
                onChange={(e) => setNewTtData({ ...newTtData, academicYear: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Timetable Title</label>
            <input
              type="text"
              required
              value={newTtData.title}
              onChange={(e) => setNewTtData({ ...newTtData, title: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div className="pt-3 flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setIsAddModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={generating}
              className="px-5 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white text-xs font-bold rounded-xl shadow-lg flex items-center space-x-2"
            >
              {generating ? (
                <span>Generating AI Timetable...</span>
              ) : (
                <>
                  <Sparkles className="h-4 w-4" />
                  <span>Generate & Save Timetable</span>
                </>
              )}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
