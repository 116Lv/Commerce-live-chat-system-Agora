import apiClient from './client.js';

export const createProductReport = (report, config = {}) => apiClient.post('/api/reports/products', report, config);

export const createUserReport = (report, config = {}) => apiClient.post('/api/reports/users', report, config);
