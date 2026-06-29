import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const getProducts = (params = {}, config = {}) =>
  apiClient.get('/api/products', { ...config, params: compactParams(params) });

export const searchProducts = (params = {}, config = {}) =>
  apiClient.get('/api/v2/products/search', { ...config, params: compactParams(params) });

export const getProduct = (productId, config = {}) => apiClient.get(`/api/products/${productId}`, config);

export const createProduct = (product, config = {}) => apiClient.post('/api/products', product, config);

export const updateProduct = (productId, product, config = {}) =>
  apiClient.patch(`/api/products/${productId}`, product, config);

export const deleteProduct = (productId, config = {}) => apiClient.delete(`/api/products/${productId}`, config);

export const uploadProductImage = (productId, image, config = {}) => {
  const formData = new FormData();
  formData.append('image', image);

  return apiClient.post(`/api/products/${productId}/images`, formData, config);
};

export const likeProduct = (productId, config = {}) =>
  apiClient.post(`/api/products/${productId}/likes`, null, config);

export const unlikeProduct = (productId, config = {}) => apiClient.delete(`/api/products/${productId}/likes`, config);

export const getMyProducts = (config = {}) => apiClient.get('/api/users/me/products', config);

export const getMyLikedProducts = (config = {}) => apiClient.get('/api/users/me/likes', config);
