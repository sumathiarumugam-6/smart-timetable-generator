import React, { useEffect, useState } from 'react';
import { leaveService } from '../services/leaveService';
import { facultyService } from '../services/facultyService';
import { useAuth } from '../context/AuthContext';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { CalendarOff, CheckCircle2, XCircle, UserCheck } from 'lucide-react';

export const LeaveManagementPage = () => {
  const [leaves, setLeaves] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { user, hasRole } = useAuth();

  const [formData, setFormData] = useState({
    facultyId: '',
    startDate: '',
    endDate: '',
    reason: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [leaveData, facData] = await Promise.all([leaveService.getAll(), facultyService.getAll()]);
      setLeaves(leaveData);
      setFaculties(facData);
      if (facData.length > 0) {
        setFormData((prev) => ({ ...prev, facultyId: facData[0].id }));
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenApply = () => {
    const today = new Date().toISOString().split('T')[0];
    setFormData({
      facultyId: faculties[0]?.id || '',
      startDate: today,
      endDate: today,
      reason: 'Attending National Academic Conference',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await leaveService.apply(formData);
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      alert('Failed to submit leave request');
    }
  };

  const handleApprove = async (id) => {
    try {
      await leaveService.approve(id);
      loadData();
      alert('Leave approved! Substitute faculty automatically assigned to timetable.');
    } catch (err) {
      alert('Failed to approve leave');
    }
  };

  const handleReject = async (id) => {
    try {
      await leaveService.reject(id);
      loadData();
    } catch (err) {
      alert('Failed to reject leave');
    }
  };

  const columns = [
    { header: 'Faculty Code', accessor: 'facultyCode', render: (row) => <span className="font-bold text-indigo-400">{row.facultyCode}</span> },
    { header: 'Faculty Name', accessor: 'facultyName', render: (row) => <span className="font-semibold">{row.facultyName}</span> },
    { header: 'Start Date', accessor: 'startDate' },
    { header: 'End Date', accessor: 'endDate' },
    { header: 'Reason', accessor: 'reason', render: (row) => <span className="text-slate-300 text-xs italic">{row.reason}</span> },
    {
      header: 'Substitute Faculty',
      accessor: 'substituteFacultyName',
      render: (row) =>
        row.substituteFacultyName ? (
          <span className="text-xs font-semibold text-emerald-400 flex items-center space-x-1">
            <UserCheck className="h-3.5 w-3.5" />
            <span>{row.substituteFacultyName}</span>
          </span>
        ) : (
          <span className="text-xs text-slate-500">Auto-Assign on Approve</span>
        ),
    },
    {
      header: 'Status',
      accessor: 'status',
      render: (row) => (
        <span
          className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
            row.status === 'APPROVED'
              ? 'bg-emerald-500/20 text-emerald-300'
              : row.status === 'REJECTED'
              ? 'bg-red-500/20 text-red-300'
              : 'bg-amber-500/20 text-amber-300'
          }`}
        >
          {row.status}
        </span>
      ),
    },
    {
      header: 'Actions',
      accessor: 'id',
      render: (row) => (
        <div className="flex items-center space-x-2">
          {row.status === 'PENDING' && hasRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_HOD') && (
            <>
              <button
                onClick={() => handleApprove(row.id)}
                className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold transition shadow"
              >
                Approve
              </button>
              <button
                onClick={() => handleReject(row.id)}
                className="px-2.5 py-1 rounded-lg bg-red-600 hover:bg-red-700 text-white text-xs font-semibold transition shadow"
              >
                Reject
              </button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <DataGridTable
        title="Faculty Leave & Substitution Management"
        subtitle="Process faculty leave applications and automatically re-assign substitute teachers."
        columns={columns}
        data={leaves}
        onAdd={handleOpenApply}
        addLabel="Apply For Leave"
      />

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Apply Leave Request">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Faculty Member</label>
            <select
              value={formData.facultyId}
              onChange={(e) => setFormData({ ...formData, facultyId: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            >
              {faculties.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name} ({f.facultyCode})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Start Date</label>
              <input
                type="date"
                required
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">End Date</label>
              <input
                type="date"
                required
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Reason for Leave</label>
            <textarea
              required
              rows={3}
              placeholder="Specify reason..."
              value={formData.reason}
              onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            ></textarea>
          </div>

          <div className="pt-2 flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-xl"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-xl shadow-lg"
            >
              Submit Application
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
