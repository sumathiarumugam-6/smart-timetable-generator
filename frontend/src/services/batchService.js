import API from './api';

export const batchService = {
  getAll: async (departmentId) => {
    const url = departmentId ? `/batches?departmentId=${departmentId}` : '/batches';
    const res = await API.get(url);
    return res.data;
  },
  create: async (data) => {
    const res = await API.post('/batches', data);
    return res.data;
  },
  update: async (id, data) => {
    const res = await API.put(`/batches/${id}`, data);
    return res.data;
  },
  delete: async (id) => {
    const res = await API.delete(`/batches/${id}`);
    return res.data;
  },
};
