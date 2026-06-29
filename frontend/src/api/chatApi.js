import apiClient from './client.js';

const compactParams = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''));

export const openChatRoom = (productId, config = {}) =>
  apiClient.post(`/api/chat/rooms/products/${productId}`, null, config);

export const getMyRooms = (config = {}) => apiClient.get('/api/chat/rooms', config);

export const getMessages = (chatRoomId, params = {}, config = {}) =>
  apiClient.get(`/api/chat/rooms/${chatRoomId}/messages`, { ...config, params: compactParams(params) });

export const markRoomRead = (chatRoomId, config = {}) =>
  apiClient.patch(`/api/chat/rooms/${chatRoomId}/read`, undefined, config);

export const uploadChatImage = (chatRoomId, image, config = {}) => {
  const formData = new FormData();
  formData.append('image', image);

  return apiClient.post(`/api/chat/rooms/${chatRoomId}/images`, formData, config);
};
