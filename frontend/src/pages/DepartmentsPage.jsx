import React, { useEffect, useState } from 'react';
import { departmentService } from '../services/departmentService';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { Edit2, Trash2 } from 'lucide-react';

export const DepartmentsPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDept, setEditingDept] = useState(null);

  const [formData, setFormData] = useState({
    code: '',
    name: '',
    hodName: '',
    building: '',
  });

  useEffect(() => {
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      const data = await departmentService.getAll();
      setDepartments(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingDept(null);
    setFormData({ code: '', name: '', hodName: '', building: '' });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (dept) => {
    setEditingDept(dept);
    setFormData({
      code: dept.code,
      name: dept.name,
      hodName: dept.hodName || '',
      building: dept.building || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingDept) {
        await departmentService.update(editingDept.id, formData);
      } else {
        await departmentService.create(formData);
      }
      setIsModalOpen(false);
      loadDepartments();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save department');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this department?')) return;
    try {
      await departmentService.delete(id);
      loadDepartments();
    } catch (err) {
      alert('Failed to delete department');
    }
  };

  const columns = [
    { header: 'Dept Code', accessor: 'code', render: (row) => <span className="font-bold text-indigo-400">{row.code}</span> },
    { header: 'Department Name', accessor: 'name', render: (row) => <span className="font-semibold">{row.name}</span> },
    { header: 'Head of Department (HOD)', accessor: 'hodName', render: (row) => row.hodName || 'Not Assigned' },
    { header: 'Building Wing', accessor: 'building', render: (row) => row.building || 'Main Campus' },
    {
      header: 'Actions',
      accessor: 'id',
      render: (row) => (
        <div className="flex items-center space-x-2">
          <button
            onClick={() => handleOpenEdit(row)}
            className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 hover:bg-indigo-500/20 transition"
          >
            <Edit2 className="h-4 w-4" />
          </button>
          <button
            onClick={() => handleDelete(row.id)}
            className="p-1.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <DataGridTable
        title="Engineering Departments"
        subtitle="Manage academic departments, HOD assignments, and building locations."
        columns={columns}
        data={departments}
        onAdd={handleOpenAdd}
        addLabel="Add Department"
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingDept ? 'Edit Department' : 'Add New Department'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Department Code</label>
            <input
              type="text"
              required
              placeholder="e.g. CSE"
              value={formData.code}
              onChange={(e) => setFormData({ ...formData, code: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Department Name</label>
            <input
              type="text"
              required
              placeholder="e.g. Computer Science & Engineering"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">HOD Name</label>
            <input
              type="text"
              placeholder="e.g. Dr. Aris Thorne"
              value={formData.hodName}
              onChange={(e) => setFormData({ ...formData, hodName: e.target.value })}
              className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Building Wing</label>
            <input
              type="text"
              placeholder="e.g. Turing Block"
              value={formData.building}
              onChange={(e) => setFormData({ ...formData, building: e.target.value })}
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
              Save Department
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
