import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { formatAuthorizationHeader } from '../../api/client.js';
import { getUserToken } from '../../auth/tokenStorage.js';
import { getSocketUrl } from './useChatSocket.js';

const NOTIFICATION_PREVIEW_LIMIT = 80;

export const CHAT_NOTIFICATION_DESTINATIONS = {
  subscribe: (userId) => `/sub/users/${userId}/chat`
};

const decodeBase64Url = (value) => {
  const normalized = String(value || '').replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');

  if (typeof globalThis.atob === 'function') {
    return globalThis.atob(padded);
  }

  if (typeof Buffer !== 'undefined') {
    return Buffer.from(padded, 'base64').toString('utf8');
  }

  return '';
};

export const getCurrentUserIdFromToken = (token = getUserToken()) => {
  try {
    const rawToken = String(token || '').replace(/^Bearer\s+/i, '');
    const [, payload] = rawToken.split('.');

    if (!payload) {
      return null;
    }

    const parsedPayload = JSON.parse(decodeBase64Url(payload));
    const userId = parsedPayload?.userId ?? parsedPayload?.id ?? parsedPayload?.sub;

    return userId == null ? null : String(userId);
  } catch {
    return null;
  }
};

export const getActiveChatRoomId = (pathname = '') => {
  const match = String(pathname).match(/^\/chat\/([^/?#]+)/);
  return match?.[1] ?? null;
};

export const createRoomMap = (rooms = []) =>
  new Map(
    (Array.isArray(rooms) ? rooms : [])
      .filter((room) => room?.chatRoomId != null)
      .map((room) => [String(room.chatRoomId), room])
  );

export const getChatMessagePreview = (message = {}) => {
  const type = String(message.messageType || '').toUpperCase();

  if (type === 'IMAGE') {
    return 'Image message';
  }

  const content = String(message.content || '').trim();

  if (!content) {
    return 'New message';
  }

  return content.length > NOTIFICATION_PREVIEW_LIMIT ? `${content.slice(0, NOTIFICATION_PREVIEW_LIMIT - 1)}...` : content;
};

export const createChatNotification = (message = {}, room, { activeChatRoomId, currentUserId } = {}) => {
  const chatRoomId = message.chatRoomId ?? room?.chatRoomId;

  if (chatRoomId == null) {
    return null;
  }

  const roomId = String(chatRoomId);

  if (activeChatRoomId != null && String(activeChatRoomId) === roomId) {
    return null;
  }

  if (currentUserId != null && message.senderId != null && String(message.senderId) === String(currentUserId)) {
    return null;
  }

  const context = message.productTitle || room?.productTitle || `Chat room #${roomId}`;
  const sender = message.senderNickname || 'Unknown sender';
  const preview = getChatMessagePreview(message);

  return {
    id: `${roomId}-${message.messageId ?? message.createdAt ?? preview}`,
    chatRoomId: roomId,
    sender,
    context,
    preview
  };
};

const parseFrameBody = (body) => {
  try {
    return JSON.parse(body);
  } catch {
    return { content: body };
  }
};

const getConnectHeaders = () => {
  const authorization = formatAuthorizationHeader(getUserToken());
  return authorization ? { Authorization: authorization } : {};
};

export default function useChatNotifications({ enabled, activeChatRoomId, currentUserId, onNotification } = {}) {
  const activeChatRoomIdRef = useRef(activeChatRoomId);
  const currentUserIdRef = useRef(currentUserId);
  const onNotificationRef = useRef(onNotification);
  const roomMapRef = useRef(new Map());

  useEffect(() => {
    activeChatRoomIdRef.current = activeChatRoomId;
  }, [activeChatRoomId]);

  useEffect(() => {
    currentUserIdRef.current = currentUserId;
  }, [currentUserId]);

  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

  useEffect(() => {
    if (!enabled || currentUserId == null) {
      return undefined;
    }

    let disposed = false;
    let subscription = null;
    const client = new Client({
      brokerURL: getSocketUrl(),
      connectHeaders: getConnectHeaders(),
      reconnectDelay: 5000,
      debug: () => {}
    });

    const subscribeUserNotifications = () => {
      if (disposed || !client.connected || subscription) {
        return;
      }

      subscription = client.subscribe(CHAT_NOTIFICATION_DESTINATIONS.subscribe(currentUserId), (frame) => {
        const message = parseFrameBody(frame.body);
        const notification = createChatNotification(message, roomMapRef.current.get(String(message.chatRoomId)), {
          activeChatRoomId: activeChatRoomIdRef.current,
          currentUserId: currentUserIdRef.current
        });

        if (notification) {
          onNotificationRef.current?.(notification);
        }
      });
    };

    client.onConnect = () => {
      subscription = null;
      subscribeUserNotifications();
    };

    client.activate();

    return () => {
      disposed = true;
      subscription?.unsubscribe();
      client.deactivate();
    };
  }, [currentUserId, enabled]);
}
