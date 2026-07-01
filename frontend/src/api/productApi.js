import apiClient from './client.js';
import { compressImageForUpload } from './imageCompression.js';

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

export const updateProductStatus = (productId, status, config = {}) =>
  apiClient.patch(`/api/products/${productId}/status`, { status }, config);

export const deleteProduct = (productId, config = {}) => apiClient.delete(`/api/products/${productId}`, config);

export const uploadProductImage = async (productId, image, config = {}) => {
  return uploadProductImages(productId, [image], config);
};

export const uploadProductImages = async (productId, images = [], config = {}) => {
  const compressedImages = await Promise.all(images.map((image) => compressImageForUpload(image)));
  const formData = new FormData();

  compressedImages.forEach((image, index) => {
    const sourceImage = images[index];
    formData.append('images', image, image.name || sourceImage?.name || `product-image-${index + 1}.jpg`);
  });

  return apiClient.post(`/api/products/${productId}/images`, formData, config);
};

export const likeProduct = (productId, config = {}) =>
  apiClient.post(`/api/products/${productId}/likes`, null, config);

export const unlikeProduct = (productId, config = {}) => apiClient.delete(`/api/products/${productId}/likes`, config);

export const getMyProducts = (config = {}) => apiClient.get('/api/users/me/products', config);

export const getMyLikedProducts = (config = {}) => apiClient.get('/api/users/me/likes', config);
