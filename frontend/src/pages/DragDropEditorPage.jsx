import React, { useEffect, useState } from 'react';
import { timetableService } from '../services/timetableService';
import { DragDropTimetableGrid } from '../components/DragDropTimetableGrid';
import { Move, CheckCircle2, AlertCircle, Save } from 'lucide-react';

export const DragDropEditorPage = () => {
  const [timetables, setTimetables] = useState([]);
  const [selectedTimetable, setSelectedTimetable] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });

  useEffect(() => {
    loadTimetables();
  }, []);

  const loadTimetables = async () => {
    try {
      const data = await timetableService.getAll();
      setTimetables(data);
      if (data.length > 0) {
        setSelectedTimetable(data[0]);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSwap = async (draggedEntry, targetDay, targetPeriodNum) => {
    if (!selectedTimetable) return;
    setMessage({ text: 'Validating swap with AI Constraint Checker...', type: 'info' });

    try {
      const updated = await timetableService.manualSwap({
        timetableId: selectedTimetable.id,
        entryId1: draggedEntry.id,
        targetDay: targetDay,
        targetPeriodNumber: targetPeriodNum,
      });

      setSelectedTimetable(updated);
      setMessage({ text: 'Swap successful! No conflicts introduced.', type: 'success' });
    } catch (err) {
      setMessage({ text: err.response?.data?.message || 'Swap rejected: Faculty or Room conflict in target slot!', type: 'error' });
    }
  };

  if (loading) {
    return (
      <div className="py-20 text-center text-slate-400">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-indigo-500 mx-auto"></div>
        <p className="mt-4 text-xs font-semibold">Loading Drag & Drop Editor...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center space-x-3">
          <div className="h-10 w-10 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
            <Move className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-2xl font-extrabold text-white">HOD Interactive Timetable Editor</h1>
            <p className="text-xs text-slate-400">Manually adjust period slots with real-time conflict checking.</p>
          </div>
        </div>

        <select
          value={selectedTimetable?.id || ''}
          onChange={(e) => {
            const tt = timetables.find((t) => t.id.toString() === e.target.value);
            setSelectedTimetable(tt);
            setMessage({ text: '', type: '' });
          }}
          className="px-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500"
        >
          {timetables.map((t) => (
            <option key={t.id} value={t.id}>
              {t.title}
            </option>
          ))}
        </select>
      </div>

      {message.text && (
        <div
          className={`p-4 rounded-xl border text-xs font-semibold flex items-center space-x-2 animate-fade-in ${
            message.type === 'success'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : message.type === 'error'
              ? 'bg-red-500/10 border-red-500/30 text-red-300'
              : 'bg-indigo-500/10 border-indigo-500/30 text-indigo-300'
          }`}
        >
          {message.type === 'success' && <CheckCircle2 className="h-4 w-4 text-emerald-400" />}
          {message.type === 'error' && <AlertCircle className="h-4 w-4 text-red-400" />}
          <span>{message.text}</span>
        </div>
      )}

      {selectedTimetable && (
        <DragDropTimetableGrid
          entries={selectedTimetable.entries || []}
          onSwap={handleSwap}
          editable={true}
        />
      )}
    </div>
  );
};
