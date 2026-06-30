import apiClient from './client.js';

export const createOffer = (chatRoomId, offer, config = {}) =>
  apiClient.post(`/api/chat/rooms/${chatRoomId}/nego-offers`, offer, config);

export const getCurrentOffer = (chatRoomId, config = {}) =>
  apiClient.get(`/api/chat/rooms/${chatRoomId}/nego-offers/current`, config);

export const acceptOffer = (offerId, config = {}) => apiClient.patch(`/api/nego-offers/${offerId}/accept`, null, config);

export const rejectOffer = (offerId, config = {}) => apiClient.patch(`/api/nego-offers/${offerId}/reject`, null, config);

export const requestOfferExtension = (offerId, config = {}) =>
  apiClient.patch(`/api/nego-offers/${offerId}/extension-request`, null, config);

export const approveOfferExtension = (offerId, config = {}) =>
  apiClient.patch(`/api/nego-offers/${offerId}/extension-approve`, null, config);

export const rejectOfferExtension = (offerId, config = {}) =>
  apiClient.patch(`/api/nego-offers/${offerId}/extension-reject`, null, config);

export const expireOffer = (offerId, config = {}) => apiClient.post(`/api/nego-offers/${offerId}/expire`, null, config);
