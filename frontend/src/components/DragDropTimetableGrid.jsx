import React, { useState } from 'react';
import { Sparkles, Coffee, Utensils, AlertTriangle, Sunset, BookOpen, FlaskConical } from 'lucide-react';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const ALL_TIME_SLOTS = [
  { num: 1, label: 'Period 1', time: '8:30 AM – 9:20 AM' },
  { num: 2, label: 'Period 2', time: '9:20 AM – 10:10 AM' },
  { type: 'MORNING_BREAK', label: 'Morning Break', time: '10:10 AM – 10:25 AM' },
  { num: 3, label: 'Period 3', time: '10:25 AM – 11:15 AM' },
  { num: 4, label: 'Period 4', time: '11:15 AM – 12:05 PM' },
  { type: 'LUNCH_BREAK', label: 'Lunch Break', time: '12:05 PM – 12:50 PM' },
  { num: 5, label: 'Period 5', time: '12:50 PM – 1:40 PM' },
  { num: 6, label: 'Period 6', time: '1:40 PM – 2:30 PM' },
  { type: 'EVENING_BREAK', label: 'Evening Break', time: '2:30 PM – 2:45 PM' },
  { num: 7, label: 'Period 7', time: '2:45 PM – 3:35 PM' },
  { num: 8, label: 'Period 8', time: '3:35 PM – 4:25 PM' }
];

export const DragDropTimetableGrid = ({ entries = [], onSwap, editable = false, searchFilter = '' }) => {
  const [draggedEntry, setDraggedEntry] = useState(null);
  const [hoveredSlot, setHoveredSlot] = useState(null);

  // Map entries into [Day][PeriodNumber]
  const entryMap = {};
  const facultyConflictCheck = {};
  const roomConflictCheck = {};
  let totalConflicts = 0;

  entries.forEach((e) => {
    // Search filter matching
    if (searchFilter) {
      const q = searchFilter.toLowerCase();
      const matches =
        (e.subjectName && e.subjectName.toLowerCase().includes(q)) ||
        (e.subjectCode && e.subjectCode.toLowerCase().includes(q)) ||
        (e.facultyName && e.facultyName.toLowerCase().includes(q)) ||
        (e.roomNumber && e.roomNumber.toLowerCase().includes(q));
      if (!matches) return;
    }

    const key = `${e.dayOfWeek}_${e.periodNumber}`;
    entryMap[key] = e;

    // Check conflicts
    const fKey = `${e.dayOfWeek}_${e.periodNumber}_${e.facultyId}`;
    const rKey = `${e.dayOfWeek}_${e.periodNumber}_${e.classroomId}`;

    facultyConflictCheck[fKey] = (facultyConflictCheck[fKey] || 0) + 1;
    roomConflictCheck[rKey] = (roomConflictCheck[rKey] || 0) + 1;

    if (facultyConflictCheck[fKey] > 1 || roomConflictCheck[rKey] > 1) {
      totalConflicts++;
    }
  });

  const handleDragStart = (entry) => {
    if (!editable) return;
    setDraggedEntry(entry);
  };

  const handleDragOver = (e, day, periodNum) => {
    e.preventDefault();
    if (!draggedEntry) return;
    setHoveredSlot(`${day}_${periodNum}`);
  };

  const handleDrop = (targetDay, targetPeriodNum) => {
    if (!draggedEntry || !onSwap) return;
    onSwap(draggedEntry, targetDay, targetPeriodNum);
    setDraggedEntry(null);
    setHoveredSlot(null);
  };

  return (
    <div className="space-y-4">
      {/* Validation Conflict Warning Banner */}
      {totalConflicts > 0 && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs font-semibold flex items-center justify-between animate-pulse">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="h-4 w-4 text-amber-400 shrink-0" />
            <span>
              <strong>Timetable Conflict Warning:</strong> Detected {totalConflicts} schedule overlap(s) in faculty or classroom assignments!
            </span>
          </div>
          <span className="px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/30 text-[10px] uppercase font-bold">
            Conflicts Detected
          </span>
        </div>
      )}

      {editable && (
        <div className="p-4 rounded-xl bg-indigo-500/10 border border-indigo-500/30 flex items-center justify-between text-xs text-indigo-300">
          <div className="flex items-center space-x-2">
            <Sparkles className="h-4 w-4 text-indigo-400" />
            <span>
              <strong className="font-semibold">Interactive Drag & Drop Mode:</strong> Drag class slots to swap periods. Conflict detector automatically validates faculty & room availability.
            </span>
          </div>
        </div>
      )}

      <div className="glass-card rounded-2xl border border-slate-800 overflow-hidden shadow-2xl print:border-none print:shadow-none">
        <div className="overflow-x-auto max-h-[70vh]">
          <table className="w-full text-center border-collapse">
            <thead className="sticky top-0 z-30 bg-slate-900 shadow-md">
              <tr className="border-b border-slate-800 text-xs font-semibold text-slate-300 uppercase tracking-wider">
                <th className="p-3.5 text-left w-32 sticky left-0 z-40 bg-slate-900 border-r border-slate-800">
                  Day / Time
                </th>
                {ALL_TIME_SLOTS.map((slot, idx) => {
                  if (slot.type === 'MORNING_BREAK') {
                    return (
                      <th key={idx} className="p-2.5 bg-amber-950/40 w-24 text-[10px] text-amber-400 border-r border-slate-800">
                        {slot.label}
                        <br />
                        <span className="text-slate-400 font-normal">{slot.time}</span>
                      </th>
                    );
                  }
                  if (slot.type === 'LUNCH_BREAK') {
                    return (
                      <th key={idx} className="p-2.5 bg-emerald-950/40 w-24 text-[10px] text-emerald-400 border-r border-slate-800">
                        {slot.label}
                        <br />
                        <span className="text-slate-400 font-normal">{slot.time}</span>
                      </th>
                    );
                  }
                  if (slot.type === 'EVENING_BREAK') {
                    return (
                      <th key={idx} className="p-2.5 bg-orange-950/40 w-24 text-[10px] text-orange-400 border-r border-slate-800">
                        {slot.label}
                        <br />
                        <span className="text-slate-400 font-normal">{slot.time}</span>
                      </th>
                    );
                  }
                  return (
                    <th key={idx} className="p-3 w-44 border-r border-slate-800">
                      {slot.label}
                      <br />
                      <span className="text-[10px] text-slate-400 font-normal">{slot.time}</span>
                    </th>
                  );
                })}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {DAYS.map((day) => (
                <tr key={day} className="hover:bg-slate-900/30 transition">
                  {/* Day Column */}
                  <td className="p-3 font-extrabold text-xs text-indigo-300 sticky left-0 z-20 bg-slate-900 text-left border-r border-slate-800">
                    {day}
                  </td>

                  {/* Slot Columns */}
                  {ALL_TIME_SLOTS.map((slot, sIdx) => {
                    // Morning Break Column
                    if (slot.type === 'MORNING_BREAK') {
                      return (
                        <td key={sIdx} className="p-2 bg-amber-500/5 text-amber-400/80 border-r border-slate-800 text-[11px] font-semibold">
                          <div className="flex flex-col items-center justify-center space-y-1 py-4">
                            <Coffee className="h-4 w-4 text-amber-400" />
                            <span className="text-[10px]">Morning Break</span>
                          </div>
                        </td>
                      );
                    }

                    // Lunch Break Column
                    if (slot.type === 'LUNCH_BREAK') {
                      return (
                        <td key={sIdx} className="p-2 bg-emerald-500/5 text-emerald-400/80 border-r border-slate-800 text-[11px] font-semibold">
                          <div className="flex flex-col items-center justify-center space-y-1 py-4">
                            <Utensils className="h-4 w-4 text-emerald-400" />
                            <span className="text-[10px]">Lunch Break</span>
                          </div>
                        </td>
                      );
                    }

                    // Evening Break Column
                    if (slot.type === 'EVENING_BREAK') {
                      return (
                        <td key={sIdx} className="p-2 bg-orange-500/5 text-orange-400/80 border-r border-slate-800 text-[11px] font-semibold">
                          <div className="flex flex-col items-center justify-center space-y-1 py-4">
                            <Sunset className="h-4 w-4 text-orange-400" />
                            <span className="text-[10px]">Evening Break</span>
                          </div>
                        </td>
                      );
                    }

                    const slotKey = `${day}_${slot.num}`;
                    const entry = entryMap[slotKey];
                    const isHovered = hoveredSlot === slotKey;

                    return (
                      <td
                        key={sIdx}
                        onDragOver={(e) => handleDragOver(e, day, slot.num)}
                        onDrop={() => handleDrop(day, slot.num)}
                        className={`p-2 border-r border-slate-800/60 min-h-[100px] transition-all relative ${
                          isHovered ? 'bg-indigo-500/20 border-2 border-indigo-500' : ''
                        }`}
                      >
                        {entry ? (
                          <div
                            draggable={editable}
                            onDragStart={() => handleDragStart(entry)}
                            className={`p-3 rounded-xl border text-left cursor-grab active:cursor-grabbing transition shadow-lg ${
                              entry.isLabSession
                                ? 'bg-purple-950/50 border-purple-500/50 text-purple-100'
                                : entry.subjectType === 'CORE'
                                ? 'bg-indigo-950/60 border-indigo-500/60 text-indigo-100'
                                : 'bg-slate-800/90 border-slate-700 text-slate-200'
                            }`}
                          >
                            <div className="flex items-center justify-between">
                              <span className="text-[10px] font-bold tracking-wider px-1.5 py-0.5 rounded bg-slate-900/80 text-slate-200 border border-slate-700">
                                {entry.subjectCode}
                              </span>
                              <span
                                className={`text-[9px] font-extrabold px-1.5 py-0.5 rounded uppercase flex items-center space-x-1 ${
                                  entry.isLabSession
                                    ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30'
                                    : 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30'
                                }`}
                              >
                                {entry.isLabSession ? (
                                  <>
                                    <FlaskConical className="h-3 w-3" />
                                    <span>LAB</span>
                                  </>
                                ) : (
                                  <>
                                    <BookOpen className="h-3 w-3" />
                                    <span>THEORY</span>
                                  </>
                                )}
                              </span>
                            </div>

                            <p className="text-xs font-bold mt-2 text-slate-100 line-clamp-1">{entry.subjectName}</p>
                            <p className="text-[11px] text-slate-300 mt-0.5 font-medium">{entry.facultyName}</p>

                            <div className="flex items-center justify-between mt-2.5 pt-1.5 border-t border-slate-700/50 text-[10px]">
                              <span className="font-bold text-amber-300 bg-amber-500/10 px-1.5 py-0.5 rounded border border-amber-500/20">
                                Room {entry.roomNumber}
                              </span>
                              <span className="text-slate-400 font-semibold">Sec {entry.batchSection}</span>
                            </div>
                          </div>
                        ) : (
                          <div className="h-full min-h-[80px] rounded-xl border border-dashed border-slate-800/70 flex items-center justify-center text-[10px] text-slate-600 font-medium">
                            Free Slot
                          </div>
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
