import API from './api';

export const dashboardService = {
  getStats: async () => {
    const res = await API.get('/dashboard/stats');
    return res.data;
  },
};
