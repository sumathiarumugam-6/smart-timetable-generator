import API from './api';

export const departmentService = {
  getAll: async () => {
    const res = await API.get('/departments');
    return res.data;
  },
  create: async (data) => {
    const res = await API.post('/departments', data);
    return res.data;
  },
  update: async (id, data) => {
    const res = await API.put(`/departments/${id}`, data);
    return res.data;
  },
  delete: async (id) => {
    const res = await API.delete(`/departments/${id}`);
    return res.data;
  },
};
