import API from './api';

export const reportService = {
  exportFacultyReport: async (facultyId) => {
    const res = await API.get(`/reports/faculty/${facultyId}/export`, { responseType: 'blob' });
    return res.data;
  },
  exportClassroomReport: async () => {
    const res = await API.get('/reports/classrooms/export', { responseType: 'blob' });
    return res.data;
  },
  exportWorkloadReport: async () => {
    const res = await API.get('/reports/workload/export', { responseType: 'blob' });
    return res.data;
  },
};
