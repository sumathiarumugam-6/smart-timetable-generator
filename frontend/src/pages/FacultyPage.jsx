import React, { useEffect, useState } from 'react';
import { facultyService } from '../services/facultyService';
import { departmentService } from '../services/departmentService';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { Edit2, Trash2 } from 'lucide-react';

export const FacultyPage = () => {
  const [faculties, setFaculties] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFaculty, setEditingFaculty] = useState(null);

  const [formData, setFormData] = useState({
    facultyCode: '',
    name: '',
    designation: '',
    departmentId: '',
    email: '',
    phone: '',
    maxWorkingHours: 20,
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [facData, deptData] = await Promise.all([facultyService.getAll(), departmentService.getAll()]);
      setFaculties(facData);
      setDepartments(deptData);
      if (deptData.length > 0) {
        setFormData((prev) => ({ ...prev, departmentId: deptData[0].id }));
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingFaculty(null);
    setFormData({
      facultyCode: `FAC_${Date.now().toString().slice(-4)}`,
      name: '',
      designation: 'Assistant Professor',
      departmentId: departments[0]?.id || '',
      email: '',
      phone: '',
      maxWorkingHours: 20,
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (fac) => {
    setEditingFaculty(fac);
    setFormData({
      facultyCode: fac.facultyCode,
      name: fac.name,
      designation: fac.designation,
      departmentId: fac.departmentId,
      email: fac.email,
      phone: fac.phone || '',
      maxWorkingHours: fac.maxWorkingHours || 20,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingFaculty) {
        await facultyService.update(editingFaculty.id, formData);
      } else {
        await facultyService.create(formData);
      }
      setIsModalOpen(false);
      loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save faculty');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this faculty member?')) return;
    try {
      await facultyService.delete(id);
      loadData();
    } catch (err) {
      alert('Failed to delete faculty');
    }
  };

  const columns = [
    { header: 'Faculty Code', accessor: 'facultyCode', render: (row) => <span className="font-bold text-indigo-400">{row.facultyCode}</span> },
    { header: 'Faculty Name', accessor: 'name', render: (row) => <span className="font-semibold">{row.name}</span> },
    { header: 'Designation', accessor: 'designation' },
    { header: 'Department', accessor: 'departmentName' },
    { header: 'Email', accessor: 'email', render: (row) => <span className="text-slate-400 text-xs">{row.email}</span> },
    {
      header: 'Max Workload',
      accessor: 'maxWorkingHours',
      render: (row) => <span className="font-semibold text-purple-300">{row.maxWorkingHours} Hrs/Week</span>,
    },
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
        title="Faculty Directory"
        subtitle="Manage faculty members, designations, department allocation, and maximum teaching load."
        columns={columns}
        data={faculties}
        onAdd={handleOpenAdd}
        addLabel="Add Faculty Member"
      />

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingFaculty ? 'Edit Faculty' : 'Add Faculty'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Faculty Code</label>
              <input
                type="text"
                required
                value={formData.facultyCode}
                onChange={(e) => setFormData({ ...formData, facultyCode: e.target.value })}
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

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Full Name</label>
            <input
              type="text"
              required
              placeholder="e.g. Prof. Alice Smith"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Designation</label>
              <select
                value={formData.designation}
                onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              >
                <option value="Professor & HOD">Professor & HOD</option>
                <option value="Professor">Professor</option>
                <option value="Associate Professor">Associate Professor</option>
                <option value="Assistant Professor">Assistant Professor</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Max Hours/Week</label>
              <input
                type="number"
                required
                value={formData.maxWorkingHours}
                onChange={(e) => setFormData({ ...formData, maxWorkingHours: parseInt(e.target.value) })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Email Address</label>
            <input
              type="email"
              required
              placeholder="alice@enggcollege.edu"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
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
              Save Faculty
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
