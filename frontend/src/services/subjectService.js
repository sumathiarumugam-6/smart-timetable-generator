import API from './api';

export const subjectService = {
  getAll: async (departmentId) => {
    const url = departmentId ? `/subjects?departmentId=${departmentId}` : '/subjects';
    const res = await API.get(url);
    return res.data;
  },
  create: async (data) => {
    const res = await API.post('/subjects', data);
    return res.data;
  },
  update: async (id, data) => {
    const res = await API.put(`/subjects/${id}`, data);
    return res.data;
  },
  delete: async (id) => {
    const res = await API.delete(`/subjects/${id}`);
    return res.data;
  },
};
