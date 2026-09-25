import API from './api';

export const classroomService = {
  getAll: async () => {
    const res = await API.get('/classrooms');
    return res.data;
  },
  create: async (data) => {
    const res = await API.post('/classrooms', data);
    return res.data;
  },
  update: async (id, data) => {
    const res = await API.put(`/classrooms/${id}`, data);
    return res.data;
  },
  delete: async (id) => {
    const res = await API.delete(`/classrooms/${id}`);
    return res.data;
  },
};
