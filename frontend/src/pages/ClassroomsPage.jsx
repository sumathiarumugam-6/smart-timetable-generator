import React, { useEffect, useState } from 'react';
import { classroomService } from '../services/classroomService';
import { DataGridTable } from '../components/DataGridTable';
import { Modal } from '../components/Modal';
import { Edit2, Trash2 } from 'lucide-react';

export const ClassroomsPage = () => {
  const [classrooms, setClassrooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState(null);

  const [formData, setFormData] = useState({
    roomNumber: '',
    building: '',
    capacity: 60,
    roomType: 'CLASSROOM',
  });

  useEffect(() => {
    loadClassrooms();
  }, []);

  const loadClassrooms = async () => {
    try {
      const data = await classroomService.getAll();
      setClassrooms(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdd = () => {
    setEditingRoom(null);
    setFormData({ roomNumber: '', building: 'Turing Block', capacity: 60, roomType: 'CLASSROOM' });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (room) => {
    setEditingRoom(room);
    setFormData({
      roomNumber: room.roomNumber,
      building: room.building,
      capacity: room.capacity,
      roomType: room.roomType,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingRoom) {
        await classroomService.update(editingRoom.id, formData);
      } else {
        await classroomService.create(formData);
      }
      setIsModalOpen(false);
      loadClassrooms();
    } catch (err) {
      alert('Failed to save classroom');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete classroom?')) return;
    try {
      await classroomService.delete(id);
      loadClassrooms();
    } catch (err) {
      alert('Failed to delete classroom');
    }
  };

  const columns = [
    { header: 'Room Number', accessor: 'roomNumber', render: (row) => <span className="font-bold text-indigo-400">{row.roomNumber}</span> },
    { header: 'Building / Block', accessor: 'building' },
    {
      header: 'Room Type',
      accessor: 'roomType',
      render: (row) => (
        <span
          className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${
            row.roomType === 'LABORATORY' ? 'bg-purple-500/20 text-purple-300' : 'bg-indigo-500/20 text-indigo-300'
          }`}
        >
          {row.roomType}
        </span>
      ),
    },
    { header: 'Capacity', accessor: 'capacity', render: (row) => `${row.capacity} Seats` },
    {
      header: 'Availability',
      accessor: 'isAvailable',
      render: (row) => (
        <span className={`text-xs font-semibold ${row.isAvailable ? 'text-emerald-400' : 'text-red-400'}`}>
          {row.isAvailable ? 'Available' : 'Maintenance'}
        </span>
      ),
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
        title="Classrooms & Laboratories"
        subtitle="Manage campus classrooms, lab facilities, seating capacities, and room allocations."
        columns={columns}
        data={classrooms}
        onAdd={handleOpenAdd}
        addLabel="Add Classroom"
      />

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingRoom ? 'Edit Room' : 'Add Room'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Room Number</label>
              <input
                type="text"
                required
                placeholder="e.g. TB-101"
                value={formData.roomNumber}
                onChange={(e) => setFormData({ ...formData, roomNumber: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Room Type</label>
              <select
                value={formData.roomType}
                onChange={(e) => setFormData({ ...formData, roomType: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              >
                <option value="CLASSROOM">CLASSROOM</option>
                <option value="LABORATORY">LABORATORY</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Building Wing</label>
              <input
                type="text"
                required
                placeholder="e.g. Turing Block"
                value={formData.building}
                onChange={(e) => setFormData({ ...formData, building: e.target.value })}
                className="w-full px-3.5 py-2 bg-slate-900 border border-slate-700 rounded-xl text-sm text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Seating Capacity</label>
              <input
                type="number"
                required
                value={formData.capacity}
                onChange={(e) => setFormData({ ...formData, capacity: parseInt(e.target.value) })}
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
              Save Room
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
