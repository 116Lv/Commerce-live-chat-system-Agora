import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const getRegions = (params = {}, config = {}) =>
  apiClient.get('/api/regions', { ...config, params: compactParams(params) });

export const updatePreferredRegions = ({ regionIds, primaryRegionId }, config = {}) =>
  apiClient.put('/api/users/me/regions', { regionIds, primaryRegionId }, config);
