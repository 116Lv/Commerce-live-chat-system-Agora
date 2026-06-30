import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  getMessages,
  getMyRooms,
  markRoomRead,
  openChatRoom,
  uploadChatImage
} from './chatApi.js';
import {
  acceptOffer,
  approveOfferExtension,
  createOffer,
  expireOffer,
  getCurrentOffer,
  rejectOffer,
  rejectOfferExtension,
  requestOfferExtension
} from './negoApi.js';
import {
  completeTrade,
  expireReservation,
  getTradeDetail,
  requestRatingMessage,
  startTrade
} from './tradeApi.js';
import { confirmPayment, getRefundStatus, preparePayment, refundPayment } from './paymentApi.js';
import { createReview, getTradeReviews } from './reviewApi.js';
import { createProductReport, createUserReport } from './reportApi.js';
import {
  CHAT_DESTINATIONS,
  flushPendingChatMessages,
  queuePendingChatMessage,
  readPendingChatMessages
} from '../features/chat/useChatSocket.js';

const captureRequest = () => {
  const requests = [];
  const adapter = (config) => {
    requests.push(config);
    return Promise.resolve({
      config,
      data: { status: 'SUCCESS', message: 'ok', data: { ok: true } },
      headers: {},
      status: 200,
      statusText: 'OK'
    });
  };

  return { requests, config: { adapter } };
};

const parseJsonBody = (data) => (typeof data === 'string' ? JSON.parse(data) : data);
const summarizeRequest = ({ method, url, params, data }) => ({ method, url, params, data: parseJsonBody(data) });

test('chat API maps room, message, read, and image endpoints', async () => {
  const { requests, config } = captureRequest();
  const image = new Blob(['image-bytes'], { type: 'image/png' });

  await openChatRoom(7, config);
  await getMyRooms(config);
  await getMessages(3, { lastMessageId: 9, size: 50 }, config);
  await markRoomRead(3, config);
  await uploadChatImage(3, image, config);

  assert.deepEqual(requests.slice(0, 4).map(summarizeRequest), [
    { method: 'post', url: '/api/chat/rooms/products/7', params: undefined, data: null },
    { method: 'get', url: '/api/chat/rooms', params: undefined, data: undefined },
    { method: 'get', url: '/api/chat/rooms/3/messages', params: { lastMessageId: 9, size: 50 }, data: undefined },
    { method: 'patch', url: '/api/chat/rooms/3/read', params: undefined, data: undefined }
  ]);
  assert.equal(requests[4].method, 'post');
  assert.equal(requests[4].url, '/api/chat/rooms/3/images');
  assert.equal(requests[4].data.get('image').size, image.size);
});

test('negotiation API maps offer creation and actions', async () => {
  const { requests, config } = captureRequest();

  await createOffer(3, { offerPrice: 12000 }, config);
  await getCurrentOffer(3, config);
  await acceptOffer(4, config);
  await rejectOffer(4, config);
  await requestOfferExtension(4, config);
  await approveOfferExtension(4, config);
  await rejectOfferExtension(4, config);
  await expireOffer(4, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'post', url: '/api/chat/rooms/3/nego-offers', params: undefined, data: { offerPrice: 12000 } },
    { method: 'get', url: '/api/chat/rooms/3/nego-offers/current', params: undefined, data: undefined },
    { method: 'patch', url: '/api/nego-offers/4/accept', params: undefined, data: null },
    { method: 'patch', url: '/api/nego-offers/4/reject', params: undefined, data: null },
    { method: 'patch', url: '/api/nego-offers/4/extension-request', params: undefined, data: null },
    { method: 'patch', url: '/api/nego-offers/4/extension-approve', params: undefined, data: null },
    { method: 'patch', url: '/api/nego-offers/4/extension-reject', params: undefined, data: null },
    { method: 'post', url: '/api/nego-offers/4/expire', params: undefined, data: null }
  ]);
});

test('trade, payment, review, and report APIs map action endpoints', async () => {
  const { requests, config } = captureRequest();

  await startTrade(7, config);
  await getTradeDetail(11, config);
  await completeTrade(11, config);
  await expireReservation(11, config);
  await requestRatingMessage(11, config);
  await preparePayment(11, config);
  await confirmPayment(22, { paymentKey: 'pk_test' }, config);
  await refundPayment(22, { reason: 'cancel' }, config);
  await getRefundStatus(22, config);
  await createReview({ tradeId: 11, rating: 5, content: '좋아요' }, config);
  await getTradeReviews(11, config);
  await createProductReport({ productId: 7, reason: '부적절' }, config);
  await createUserReport({ reportedUserId: 9, reason: '비매너' }, config);

  assert.deepEqual(requests.map(summarizeRequest), [
    { method: 'post', url: '/api/trades/products/7', params: undefined, data: null },
    { method: 'get', url: '/api/trades/11', params: undefined, data: undefined },
    { method: 'post', url: '/api/trades/11/complete', params: undefined, data: null },
    { method: 'post', url: '/api/trades/11/expire-reservation', params: undefined, data: null },
    { method: 'post', url: '/api/trades/11/rating-request-message', params: undefined, data: null },
    { method: 'post', url: '/api/payments/trades/11/prepare', params: undefined, data: null },
    { method: 'post', url: '/api/payments/22/confirm', params: undefined, data: { paymentKey: 'pk_test' } },
    { method: 'post', url: '/api/payments/22/refund', params: undefined, data: { reason: 'cancel' } },
    { method: 'get', url: '/api/payments/22/refund', params: undefined, data: undefined },
    { method: 'post', url: '/api/reviews', params: undefined, data: { tradeId: 11, rating: 5, content: '좋아요' } },
    { method: 'get', url: '/api/trades/11/reviews', params: undefined, data: undefined },
    { method: 'post', url: '/api/reports/products', params: undefined, data: { productId: 7, reason: '부적절' } },
    { method: 'post', url: '/api/reports/users', params: undefined, data: { reportedUserId: 9, reason: '비매너' } }
  ]);
});

test('chat socket constants follow backend STOMP conventions', () => {
  assert.equal(CHAT_DESTINATIONS.endpoint, '/ws');
  assert.equal(CHAT_DESTINATIONS.subscribe(3), '/sub/chat/3');
  assert.equal(CHAT_DESTINATIONS.publish(3), '/pub/chat/3/messages');
});

test('disconnected chat messages are queued for STOMP retry without HTTP fallback guessing', () => {
  const storage = new Map();
  const stored = {
    getItem: (key) => storage.get(key) ?? null,
    setItem: (key, value) => storage.set(key, value),
    removeItem: (key) => storage.delete(key)
  };
  const queued = queuePendingChatMessage(3, 'hello', stored);

  assert.equal(queued.content, 'hello');
  assert.equal(queued.chatRoomId, '3');
  assert.equal(queued.pending, true);
  assert.equal(readPendingChatMessages(3, stored).length, 1);

  const published = [];
  const flushed = flushPendingChatMessages(3, (message) => published.push(message), stored);

  assert.equal(flushed.sentCount, 1);
  assert.equal(flushed.failedCount, 0);
  assert.equal(flushed.pendingCount, 0);
  assert.deepEqual(published, [{ clientMessageId: queued.clientMessageId, content: 'hello', messageType: 'TEXT' }]);
  assert.deepEqual(readPendingChatMessages(3, stored), []);
});
