import React, { useEffect, useState } from 'react';
import { subjectService } from '../services/subjectService';
import { departmentService } from '../services/departmentService';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { Edit2, Trash2 } from 'lucide-react';

export const SubjectsPage = () => {
  const [subjects, setSubjects] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSubject, setEditingSubject] = useState(null);

  const [formData, setFormData] = useState({
    subjectCode: '',
    subjectName: '',
    credits: 4,
    subjectType: 'CORE',
    semester: 5,
    weeklyPeriods: 3,
    departmentId: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [subData, deptData] = await Promise.all([subjectService.getAll(), departmentService.getAll()]);
      setSubjects(subData);
      setDepartments(deptData);
      if (deptData.length > 0) setFormData((prev) => ({ ...prev, departmentId: deptData[0].id }));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingSubject(null);
    setFormData({
      subjectCode: 'CS' + Math.floor(100 + Math.random() * 900),
      subjectName: '',
      credits: 4,
      subjectType: 'CORE',
      semester: 5,
      weeklyPeriods: 3,
      departmentId: departments[0]?.id || '',
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (sub) => {
    setEditingSubject(sub);
    setFormData({
      subjectCode: sub.subjectCode,
      subjectName: sub.subjectName,
      credits: sub.credits,
      subjectType: sub.subjectType,
      semester: sub.semester,
      weeklyPeriods: sub.weeklyPeriods || 3,
      departmentId: sub.departmentId,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingSubject) {
        await subjectService.update(editingSubject.id, formData);
      } else {
        await subjectService.create(formData);
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      alert('Failed to save subject');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this subject?')) return;
    try {
      await subjectService.delete(id);
      loadData();
    } catch (err) {
      alert('Failed to delete subject');
    }
  };

  const columns = [
    { header: 'Subject Code', accessor: 'subjectCode', render: (row) => <span className="font-bold text-indigo-400">{row.subjectCode}</span> },
    { header: 'Subject Name', accessor: 'subjectName', render: (row) => <span className="font-semibold">{row.subjectName}</span> },
    {
      header: 'Type',
      accessor: 'subjectType',
      render: (row) => (
        <span
          className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
            row.subjectType === 'LAB'
              ? 'bg-purple-500/20 text-purple-300'
              : row.subjectType === 'CORE'
              ? 'bg-indigo-500/20 text-indigo-300'
              : 'bg-emerald-500/20 text-emerald-300'
          }`}
        >
          {row.subjectType}
        </span>
      ),
    },
    { header: 'Semester', accessor: 'semester', render: (row) => `Sem ${row.semester}` },
    { header: 'Credits', accessor: 'credits', render: (row) => `${row.credits} Credits` },
    { header: 'Periods / Wk', accessor: 'weeklyPeriods', render: (row) => `${row.weeklyPeriods} Periods` },
    { header: 'Department', accessor: 'departmentName' },
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
        title="Curriculum Subjects"
        subtitle="Manage theory courses, laboratories, credits, and weekly period allocation."
        columns={columns}
        data={subjects}
        onAdd={handleOpenAdd}
        addLabel="Add Subject"
      />

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingSubject ? 'Edit Subject' : 'Add Subject'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Subject Code</label>
              <input
                type="text"
                required
                value={formData.subjectCode}
                onChange={(e) => setFormData({ ...formData, subjectCode: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Subject Type</label>
              <select
                value={formData.subjectType}
                onChange={(e) => setFormData({ ...formData, subjectType: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              >
                <option value="CORE">CORE</option>
                <option value="THEORY">THEORY</option>
                <option value="LAB">LAB</option>
                <option value="ELECTIVE">ELECTIVE</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Subject Name</label>
            <input
              type="text"
              required
              placeholder="e.g. Data Structures & Algorithms"
              value={formData.subjectName}
              onChange={(e) => setFormData({ ...formData, subjectName: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Credits</label>
              <input
                type="number"
                required
                value={formData.credits}
                onChange={(e) => setFormData({ ...formData, credits: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
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
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Weekly Periods</label>
              <input
                type="number"
                required
                value={formData.weeklyPeriods}
                onChange={(e) => setFormData({ ...formData, weeklyPeriods: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
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
              Save Subject
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
