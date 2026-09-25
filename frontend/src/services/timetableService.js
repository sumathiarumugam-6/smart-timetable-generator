import API from './api';

export const timetableService = {
  generate: async (data) => {
    const res = await API.post('/timetables/generate', data);
    return res.data;
  },

  getAll: async () => {
    const res = await API.get('/timetables');
    return res.data;
  },

  getById: async (id) => {
    const res = await API.get(`/timetables/${id}`);
    return res.data;
  },

  manualSwap: async (swapData) => {
    const res = await API.post('/timetables/manual-swap', swapData);
    return res.data;
  },

  updateStatus: async (id, status) => {
    const res = await API.patch(`/timetables/${id}/status?status=${status}`);
    return res.data;
  },

  delete: async (id) => {
    const res = await API.delete(`/timetables/${id}`);
    return res.data;
  },
};
