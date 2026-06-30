import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import {
  buildChatPublishPayload,
  CHAT_DELIVERY_STATUS,
  createPendingChatMessage,
  flushPendingChatMessages,
  publishChatMessage,
  queuePendingChatMessage,
  readPendingChatMessages,
  retryPendingChatMessage
} from './useChatSocket.js';

const createStorage = () => {
  const values = new Map();

  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: (key) => values.delete(key)
  };
};

test('pending chat messages keep stable client ids and are upserted in storage', () => {
  const storage = createStorage();
  const first = queuePendingChatMessage(3, 'hello', storage, { clientMessageId: 'client-1' });
  const second = queuePendingChatMessage(3, { ...first, deliveryStatus: CHAT_DELIVERY_STATUS.FAILED }, storage);

  assert.equal(first.clientMessageId, 'client-1');
  assert.equal(first.deliveryStatus, CHAT_DELIVERY_STATUS.PENDING);
  assert.equal(second.clientMessageId, 'client-1');
  assert.equal(second.deliveryStatus, CHAT_DELIVERY_STATUS.FAILED);
  assert.equal(readPendingChatMessages(3, storage).length, 1);
});

test('publishChatMessage avoids disconnected clients and categorizes publish errors', () => {
  const message = createPendingChatMessage({ chatRoomId: 3, content: 'hello', clientMessageId: 'client-2' });
  const disconnected = publishChatMessage({ connected: false }, 3, message);
  const failed = publishChatMessage(
    {
      connected: true,
      publish: () => {
        throw new Error('socket closed');
      }
    },
    3,
    message
  );

  assert.equal(disconnected.status, CHAT_DELIVERY_STATUS.QUEUED);
  assert.equal(failed.status, CHAT_DELIVERY_STATUS.FAILED);
  assert.match(failed.errorMessage, /socket closed/);
});

test('connected publish succeeds without returning a durable optimistic message', () => {
  const storage = createStorage();
  const message = createPendingChatMessage({ chatRoomId: 3, content: 'hello', clientMessageId: 'client-6' });
  const published = [];
  const result = publishChatMessage(
    {
      connected: true,
      publish: (frame) => published.push(JSON.parse(frame.body))
    },
    3,
    message
  );

  assert.equal(result.status, CHAT_DELIVERY_STATUS.PUBLISHED);
  assert.equal(result.message, null);
  assert.equal(readPendingChatMessages(3, storage).length, 0);
  assert.deepEqual(published, [buildChatPublishPayload(message)]);
});

test('disconnected queue keeps a local message visible until a later publish attempt', () => {
  const storage = createStorage();
  const queued = queuePendingChatMessage(3, 'offline hello', storage, { clientMessageId: 'client-7' });
  const result = publishChatMessage({ connected: false }, 3, queued);

  assert.equal(result.status, CHAT_DELIVERY_STATUS.QUEUED);
  assert.equal(queued.deliveryStatus, CHAT_DELIVERY_STATUS.PENDING);
  assert.equal(readPendingChatMessages(3, storage).length, 1);
});

test('flushPendingChatMessages removes only published messages and leaves failed retryable messages', () => {
  const storage = createStorage();

  queuePendingChatMessage(3, 'first', storage, { clientMessageId: 'client-3' });
  queuePendingChatMessage(3, 'second', storage, { clientMessageId: 'client-4' });

  const published = [];
  const result = flushPendingChatMessages(
    3,
    (payload) => {
      published.push(payload);
      if (payload.clientMessageId === 'client-4') {
        throw new Error('publish failed');
      }
    },
    storage
  );

  assert.equal(result.sentCount, 1);
  assert.equal(result.failedCount, 1);
  assert.equal(result.pendingCount, 1);
  assert.deepEqual(
    published.map((payload) => payload.clientMessageId),
    ['client-3', 'client-4']
  );
  assert.deepEqual(
    readPendingChatMessages(3, storage).map((message) => [message.clientMessageId, message.deliveryStatus]),
    [['client-4', CHAT_DELIVERY_STATUS.FAILED]]
  );
});

test('retryPendingChatMessage reuses the same client id without duplicating visible messages', () => {
  const storage = createStorage();
  const failed = queuePendingChatMessage(3, 'retry me', storage, {
    clientMessageId: 'client-5',
    deliveryStatus: CHAT_DELIVERY_STATUS.FAILED
  });
  const published = [];
  const result = retryPendingChatMessage(
    3,
    failed,
    {
      connected: true,
      publish: (frame) => published.push(JSON.parse(frame.body))
    },
    storage
  );

  assert.equal(result.message, null);
  assert.deepEqual(result.removeClientMessageIds, ['client-5']);
  assert.equal(result.pendingCount, 0);
  assert.equal(readPendingChatMessages(3, storage).length, 0);
  assert.deepEqual(published, [buildChatPublishPayload(failed)]);
});

test('flushPendingChatMessages reports published local ids so visible entries can be removed', () => {
  const storage = createStorage();

  queuePendingChatMessage(3, 'flush me', storage, { clientMessageId: 'client-8' });

  const result = flushPendingChatMessages(3, () => {}, storage);

  assert.deepEqual(result.sentClientMessageIds, ['client-8']);
  assert.equal(result.pendingCount, 0);
  assert.equal(readPendingChatMessages(3, storage).length, 0);
});

test('chat socket connect headers reuse the shared bearer formatter', () => {
  const source = readFileSync(new URL('./useChatSocket.js', import.meta.url), 'utf8');

  assert.match(source, /formatAuthorizationHeader/);
  assert.doesNotMatch(source, /startsWith\('bearer/);
});
