import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const getRegions = (params = {}, config = {}) =>
  apiClient.get('/api/regions', { ...config, params: compactParams(params) });

export const getSidoList = (config = {}) => apiClient.get('/api/regions/sido', config);

export const getSigunguList = (sido, config = {}) =>
  apiClient.get('/api/regions/sigungu', { ...config, params: compactParams({ sido }) });

export const getDongList = (sido, sigungu, config = {}) =>
  apiClient.get('/api/regions/dong', { ...config, params: compactParams({ sido, sigungu }) });

export const updatePreferredRegions = ({ regionIds, primaryRegionId }, config = {}) =>
  apiClient.put('/api/users/me/regions', { regionIds, primaryRegionId }, config);
