import React, { useEffect, useState } from 'react';
import { batchService } from '../services/batchService';
import { departmentService } from '../services/departmentService';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { Edit2, Trash2 } from 'lucide-react';

export const BatchesPage = () => {
  const [batches, setBatches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingBatch, setEditingBatch] = useState(null);

  const [formData, setFormData] = useState({
    academicYear: '2025-2026',
    departmentId: '',
    section: 'A',
    semester: 5,
    studentCount: 60,
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [batchData, deptData] = await Promise.all([batchService.getAll(), departmentService.getAll()]);
      setBatches(batchData);
      setDepartments(deptData);
      if (deptData.length > 0) setFormData((prev) => ({ ...prev, departmentId: deptData[0].id }));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingBatch(null);
    setFormData({
      academicYear: '2025-2026',
      departmentId: departments[0]?.id || '',
      section: 'A',
      semester: 5,
      studentCount: 60,
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (b) => {
    setEditingBatch(b);
    setFormData({
      academicYear: b.academicYear,
      departmentId: b.departmentId,
      section: b.section,
      semester: b.semester,
      studentCount: b.studentCount,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingBatch) {
        await batchService.update(editingBatch.id, formData);
      } else {
        await batchService.create(formData);
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      alert('Failed to save batch');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete batch?')) return;
    try {
      await batchService.delete(id);
      loadData();
    } catch (err) {
      alert('Failed to delete batch');
    }
  };

  const columns = [
    { header: 'Academic Year', accessor: 'academicYear', render: (row) => <span className="font-bold text-indigo-400">{row.academicYear}</span> },
    { header: 'Department', accessor: 'departmentName' },
    { header: 'Semester', accessor: 'semester', render: (row) => `Sem ${row.semester}` },
    { header: 'Section', accessor: 'section', render: (row) => <span className="font-bold">Sec {row.section}</span> },
    { header: 'Student Strength', accessor: 'studentCount', render: (row) => `${row.studentCount} Students` },
    {
      header: 'Actions',
      accessor: 'id',
      render: (row) => (
        <div className="flex items-center space-x-2">
          <button onClick={() => handleOpenEdit(row)} className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20">
            <Edit2 className="h-4 w-4" />
          </button>
          <button onClick={() => handleDelete(row.id)} className="p-1.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20">
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <DataGridTable
        title="Student Batches & Sections"
        subtitle="Manage student cohorts, academic year sections, and semester divisions."
        columns={columns}
        data={batches}
        onAdd={handleOpenAdd}
        addLabel="Add Batch Section"
      />

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingBatch ? 'Edit Batch' : 'Add Batch'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Academic Year</label>
              <input
                type="text"
                required
                value={formData.academicYear}
                onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Department</label>
              <select
                value={formData.departmentId}
                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              >
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Semester</label>
              <input
                type="number"
                required
                value={formData.semester}
                onChange={(e) => setFormData({ ...formData, semester: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Section</label>
              <input
                type="text"
                required
                placeholder="A, B, C"
                value={formData.section}
                onChange={(e) => setFormData({ ...formData, section: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Students</label>
              <input
                type="number"
                required
                value={formData.studentCount}
                onChange={(e) => setFormData({ ...formData, studentCount: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
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
              Save Batch
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
