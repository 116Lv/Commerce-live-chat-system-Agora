import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { getUserToken } from '../../auth/tokenStorage.js';

export const CHAT_DESTINATIONS = {
  endpoint: '/ws',
  subscribe: (chatRoomId) => `/sub/chat/${chatRoomId}`,
  publish: (chatRoomId) => `/pub/chat/${chatRoomId}/messages`
};

const pendingKey = (chatRoomId) => `agora:chat:pending:${chatRoomId}`;

const getStorage = (storage) => storage ?? globalThis.localStorage;

export const readPendingChatMessages = (chatRoomId, storage) => {
  try {
    const stored = getStorage(storage)?.getItem(pendingKey(chatRoomId));
    const messages = stored ? JSON.parse(stored) : [];
    return Array.isArray(messages) ? messages : [];
  } catch {
    return [];
  }
};

const writePendingChatMessages = (chatRoomId, messages, storage) => {
  const targetStorage = getStorage(storage);

  if (!targetStorage) {
    return;
  }

  if (messages.length === 0) {
    targetStorage.removeItem(pendingKey(chatRoomId));
  } else {
    targetStorage.setItem(pendingKey(chatRoomId), JSON.stringify(messages));
  }
};

export const queuePendingChatMessage = (chatRoomId, content, storage) => {
  const message = {
    clientMessageId: `pending-${Date.now()}-${Math.random().toString(36).slice(2)}`,
    chatRoomId: String(chatRoomId),
    content,
    createdAt: new Date().toISOString(),
    pending: true
  };
  const messages = [...readPendingChatMessages(chatRoomId, storage), message];
  writePendingChatMessages(chatRoomId, messages, storage);

  return message;
};

export const flushPendingChatMessages = (chatRoomId, publish, storage) => {
  const messages = readPendingChatMessages(chatRoomId, storage);
  let sentCount = 0;

  for (const message of messages) {
    publish({ content: message.content });
    sentCount += 1;
  }

  writePendingChatMessages(chatRoomId, messages.slice(sentCount), storage);
  return sentCount;
};

const getSocketUrl = () => {
  const apiBase = import.meta.env?.VITE_API_BASE_URL || 'http://127.0.0.1:8080';
  return new URL(CHAT_DESTINATIONS.endpoint, apiBase).toString().replace(/^http/, 'ws');
};

const getConnectHeaders = () => {
  const token = getUserToken();
  return token ? { Authorization: token.toLowerCase().startsWith('bearer ') ? token : `Bearer ${token}` } : {};
};

export default function useChatSocket(chatRoomId, onMessage) {
  const clientRef = useRef(null);
  const messageHandlerRef = useRef(onMessage);
  const [state, setState] = useState(() => ({
    connected: false,
    error: null,
    pendingCount: chatRoomId ? readPendingChatMessages(chatRoomId).length : 0
  }));

  useEffect(() => {
    messageHandlerRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!chatRoomId) {
      return undefined;
    }

    let disposed = false;
    setState((current) => ({ ...current, pendingCount: readPendingChatMessages(chatRoomId).length }));
    const client = new Client({
      brokerURL: getSocketUrl(),
      connectHeaders: getConnectHeaders(),
      reconnectDelay: 5000,
      debug: () => {}
    });

    client.onConnect = () => {
      if (disposed) {
        return;
      }

      client.subscribe(CHAT_DESTINATIONS.subscribe(chatRoomId), (frame) => {
        try {
          messageHandlerRef.current?.(JSON.parse(frame.body));
        } catch {
          messageHandlerRef.current?.({ content: frame.body });
        }
      });

      try {
        flushPendingChatMessages(chatRoomId, (payload) => {
          client.publish({
            destination: CHAT_DESTINATIONS.publish(chatRoomId),
            body: JSON.stringify(payload)
          });
        });
        setState({ connected: true, error: null, pendingCount: readPendingChatMessages(chatRoomId).length });
      } catch {
        setState({
          connected: true,
          error: '보관된 메시지를 다시 보내지 못했어요.',
          pendingCount: readPendingChatMessages(chatRoomId).length
        });
      }
    };

    client.onStompError = (frame) => {
      if (!disposed) {
        setState((current) => ({ ...current, connected: false, error: frame.headers?.message || '채팅 연결 오류' }));
      }
    };

    client.onWebSocketError = () => {
      if (!disposed) {
        setState((current) => ({ ...current, connected: false, error: '실시간 연결이 끊겼어요.' }));
      }
    };

    client.onDisconnect = () => {
      if (!disposed) {
        setState((current) => ({ ...current, connected: false }));
      }
    };

    clientRef.current = client;
    client.activate();

    return () => {
      disposed = true;
      clientRef.current = null;
      client.deactivate();
    };
  }, [chatRoomId]);

  const sendMessage = useCallback(
    (content) => {
      const client = clientRef.current;

      if (!client?.connected || !chatRoomId) {
        queuePendingChatMessage(chatRoomId, content);
        const pendingCount = readPendingChatMessages(chatRoomId).length;
        setState((current) => ({
          ...current,
          connected: false,
          error: '실시간 연결이 회복되면 메시지를 보낼게요.',
          pendingCount
        }));
        return { queued: true, pendingCount };
      }

      client.publish({
        destination: CHAT_DESTINATIONS.publish(chatRoomId),
        body: JSON.stringify({ content })
      });
      return { queued: false, pendingCount: readPendingChatMessages(chatRoomId).length };
    },
    [chatRoomId]
  );

  return useMemo(() => ({ ...state, sendMessage }), [state, sendMessage]);
}
