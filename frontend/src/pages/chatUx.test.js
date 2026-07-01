import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';
import { test } from 'node:test';

const __dirname = dirname(fileURLToPath(import.meta.url));
const readSource = (relativePath) => readFileSync(resolve(__dirname, relativePath), 'utf8');

test('ChatRoomPage renders system, image, ownership, and retry markers', () => {
  const source = readSource('./ChatRoomPage.jsx');

  assert.match(source, /isSystemMessage/);
  assert.match(source, /message-item-system/);
  assert.match(source, /isSafeChatImageUrl/);
  assert.match(source, /chat-image-preview/);
  assert.match(source, /message-item-own/);
  assert.match(source, /handleRetryMessage/);
  assert.match(source, /deliveryStatus === CHAT_DELIVERY_STATUS\.FAILED/);
  assert.match(source, /socket\.retryMessage/);
});

test('ChatRoomPage does not persist durable optimistic bubbles after connected publish success', () => {
  const source = readSource('./ChatRoomPage.jsx');

  assert.match(source, /result\.message && !result\.transient/);
  assert.match(source, /removeClientMessageIds/);
  assert.match(source, /removeLocalMessagesByClientId/);
  assert.match(source, /reconcileServerEchoWithLocalMessages/);
});

test('ChatRoomPage validates image URLs before rendering inline previews', () => {
  const source = readSource('./ChatRoomPage.jsx');

  assert.match(source, /new URL\(value, window\.location\.origin\)/);
  assert.match(source, /safeProtocols\.has\(url\.protocol\)/);
  assert.match(source, /javascript:/);
  assert.match(source, /Image link unavailable/);
});

test('backend exposes uploaded chat images through a web resource handler', () => {
  const source = readSource('../../../src/main/java/com/team7/agora/global/config/UploadResourceConfig.java');

  assert.match(source, /WebMvcConfigurer/);
  assert.match(source, /addResourceHandlers/);
  assert.match(source, /\/uploads\/\*\*/);
  assert.match(source, /file:/);
});

test('ChatRoomsPage renders metadata-rich room rows without manual product ID opening', () => {
  const source = readSource('./ChatRoomsPage.jsx');

  assert.match(source, /productThumbnailUrl/);
  assert.match(source, /productTitle/);
  assert.match(source, /productPrice/);
  assert.match(source, /sellerNickname/);
  assert.match(source, /buyerNickname/);
  assert.match(source, /lastMessagePreview/);
  assert.match(source, /lastMessageType/);
  assert.match(source, /lastMessageCreatedAt/);
  assert.match(source, /unreadCount/);
  assert.match(source, /chat-room-unread-badge/);
  assert.doesNotMatch(source, /openChatRoom/);
  assert.doesNotMatch(source, /handleOpenRoom/);
  assert.doesNotMatch(source, /type="number"/);
});

test('ChatRoomPage can render a compact product header from room metadata', () => {
  const source = readSource('./ChatRoomPage.jsx');

  assert.match(source, /getMyRooms/);
  assert.match(source, /chatRoomMetadata/);
  assert.match(source, /chat-room-product-card/);
  assert.match(source, /productThumbnailUrl/);
  assert.match(source, /productTitle/);
  assert.match(source, /productPrice/);
  assert.match(source, /sellerNickname/);
  assert.match(source, /buyerNickname/);
});
