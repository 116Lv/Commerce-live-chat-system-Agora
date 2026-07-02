import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import {
  createChatNotification,
  createRoomMap,
  getActiveChatRoomId,
  getChatMessagePreview,
  getCurrentUserIdFromToken
} from './useChatNotifications.js';

const createToken = (payload) => {
  const encode = (value) => Buffer.from(JSON.stringify(value)).toString('base64url');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
};

test('getCurrentUserIdFromToken reads the shared JWT subject shape', () => {
  assert.equal(getCurrentUserIdFromToken(createToken({ sub: '42', nickname: 'buyer' })), '42');
  assert.equal(getCurrentUserIdFromToken(createToken({ userId: 7 })), '7');
  assert.equal(getCurrentUserIdFromToken('not-a-token'), null);
});

test('getActiveChatRoomId recognizes only concrete chat room routes', () => {
  assert.equal(getActiveChatRoomId('/chat/3'), '3');
  assert.equal(getActiveChatRoomId('/chat/3?tab=messages'), '3');
  assert.equal(getActiveChatRoomId('/chat'), null);
  assert.equal(getActiveChatRoomId('/products/3'), null);
});

test('createChatNotification includes sender, room context, and message preview', () => {
  const room = { chatRoomId: 3, productTitle: 'Vintage camera' };
  const notification = createChatNotification(
    {
      messageId: 9,
      chatRoomId: 3,
      senderId: 12,
      senderNickname: 'seller',
      content: 'Is this still available?',
      messageType: 'TEXT'
    },
    room,
    { activeChatRoomId: null, currentUserId: '11' }
  );

  assert.equal(notification.chatRoomId, '3');
  assert.equal(notification.sender, 'seller');
  assert.equal(notification.context, 'Vintage camera');
  assert.equal(notification.preview, 'Is this still available?');
});

test('createChatNotification uses notification product title without room metadata', () => {
  const notification = createChatNotification(
    {
      messageId: 10,
      chatRoomId: 5,
      senderId: 12,
      senderNickname: 'seller',
      productTitle: 'Film camera',
      content: '첫 메시지입니다.',
      messageType: 'TEXT'
    },
    null,
    { activeChatRoomId: null, currentUserId: '11' }
  );

  assert.equal(notification.context, 'Film camera');
  assert.equal(notification.preview, '첫 메시지입니다.');
});


test('createChatNotification suppresses own messages and the active chat room', () => {
  const room = { chatRoomId: 3, productTitle: 'Vintage camera' };
  const message = { messageId: 9, chatRoomId: 3, senderId: 12, content: 'hello' };

  assert.equal(createChatNotification(message, room, { activeChatRoomId: '3', currentUserId: '11' }), null);
  assert.equal(createChatNotification(message, room, { activeChatRoomId: null, currentUserId: '12' }), null);
});

test('chat notification helpers fall back to room id and image previews', () => {
  const rooms = createRoomMap([{ chatRoomId: 8 }]);
  const notification = createChatNotification(
    { messageId: 4, chatRoomId: 8, senderId: 2, messageType: 'IMAGE', content: '/uploads/chat/a.jpg' },
    rooms.get('8'),
    { currentUserId: '1' }
  );

  assert.equal(getChatMessagePreview({ messageType: 'IMAGE', content: '/uploads/chat/a.jpg' }), 'Image message');
  assert.equal(notification.context, 'Chat room #8');
  assert.equal(notification.preview, 'Image message');
});

test('global notification code reuses shared chat socket auth helpers and subscribes to user notifications', () => {
  const source = readFileSync(new URL('./useChatNotifications.js', import.meta.url), 'utf8');
  const layoutSource = readFileSync(new URL('../../layouts/UserLayout.jsx', import.meta.url), 'utf8');

  assert.match(source, /formatAuthorizationHeader/);
  assert.match(source, /getUserToken/);
  assert.match(source, /CHAT_NOTIFICATION_DESTINATIONS\.subscribe/);
  assert.match(source, /\/sub\/users\/\$\{userId\}\/chat/);
  assert.match(source, /reconnectDelay: 5000/);
  assert.match(source, /client\.onConnect/);
  assert.match(layoutSource, /GlobalChatNotification/);
});
