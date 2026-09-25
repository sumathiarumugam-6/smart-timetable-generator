import API from './api';

export const facultyService = {
  getAll: async (departmentId) => {
    const url = departmentId ? `/faculties?departmentId=${departmentId}` : '/faculties';
    const res = await API.get(url);
    return res.data;
  },
  create: async (data) => {
    const res = await API.post('/faculties', data);
    return res.data;
  },
  update: async (id, data) => {
    const res = await API.put(`/faculties/${id}`, data);
    return res.data;
  },
  delete: async (id) => {
    const res = await API.delete(`/faculties/${id}`);
    return res.data;
  },
};
