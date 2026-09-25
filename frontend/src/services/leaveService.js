import API from './api';

export const leaveService = {
  getAll: async (facultyId) => {
    const url = facultyId ? `/leaves?facultyId=${facultyId}` : '/leaves';
    const res = await API.get(url);
    return res.data;
  },
  apply: async (data) => {
    const res = await API.post('/leaves/apply', data);
    return res.data;
  },
  approve: async (id) => {
    const res = await API.put(`/leaves/${id}/approve`);
    return res.data;
  },
  reject: async (id) => {
    const res = await API.put(`/leaves/${id}/reject`);
    return res.data;
  },
};
