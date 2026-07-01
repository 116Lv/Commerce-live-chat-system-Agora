import apiClient from './client.js';

export const getRealtimePopularKeywords = (limit = 10, config = {}) =>
  apiClient.get('/api/v1/search/popular', { ...config, params: { limit } });

export const getDailyPopularKeywords = (limit = 10, config = {}) =>
  apiClient.get('/api/search/keywords/daily', { ...config, params: { limit } });

export const getWeeklyPopularKeywords = (limit = 10, config = {}) =>
  apiClient.get('/api/search/keywords/weekly', { ...config, params: { limit } });
