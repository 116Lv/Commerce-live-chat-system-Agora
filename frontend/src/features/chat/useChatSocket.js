import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { getUserToken } from '../../auth/tokenStorage.js';
import { formatAuthorizationHeader } from '../../api/client.js';

export const CHAT_DESTINATIONS = {
  endpoint: '/ws',
  subscribe: (chatRoomId) => `/sub/chat/${chatRoomId}`,
  publish: (chatRoomId) => `/pub/chat/${chatRoomId}/messages`
};

export const CHAT_DELIVERY_STATUS = {
  PENDING: 'pending',
  QUEUED: 'queued',
  FAILED: 'failed',
  PUBLISHED: 'published'
};

const pendingKey = (chatRoomId) => `agora:chat:pending:${chatRoomId}`;

const getStorage = (storage) => storage ?? globalThis.localStorage;

const createClientMessageId = () => `pending-${Date.now()}-${Math.random().toString(36).slice(2)}`;

const getErrorMessage = (error, fallback = 'Message could not be sent.') =>
  error?.message || (typeof error === 'string' ? error : fallback);

const normalizePendingMessage = (message) => {
  const deliveryStatus =
    message.deliveryStatus || (message.failed ? CHAT_DELIVERY_STATUS.FAILED : CHAT_DELIVERY_STATUS.PENDING);

  return {
    ...message,
    chatRoomId: String(message.chatRoomId),
    messageType: message.messageType || 'TEXT',
    deliveryStatus,
    pending: deliveryStatus === CHAT_DELIVERY_STATUS.PENDING || deliveryStatus === CHAT_DELIVERY_STATUS.QUEUED,
    failed: deliveryStatus === CHAT_DELIVERY_STATUS.FAILED
  };
};

export const readPendingChatMessages = (chatRoomId, storage) => {
  try {
    const stored = getStorage(storage)?.getItem(pendingKey(chatRoomId));
    const messages = stored ? JSON.parse(stored) : [];
    return Array.isArray(messages) ? messages.map(normalizePendingMessage) : [];
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

export const createPendingChatMessage = ({
  chatRoomId,
  content,
  messageType = 'TEXT',
  clientMessageId = createClientMessageId(),
  deliveryStatus = CHAT_DELIVERY_STATUS.PENDING,
  errorMessage,
  createdAt = new Date().toISOString()
}) =>
  normalizePendingMessage({
    clientMessageId,
    chatRoomId: String(chatRoomId),
    content,
    messageType,
    createdAt,
    deliveryStatus,
    errorMessage,
    source: 'local'
  });

const upsertPendingChatMessage = (chatRoomId, message, storage) => {
  const normalized = normalizePendingMessage({ ...message, chatRoomId: String(chatRoomId) });
  const messages = readPendingChatMessages(chatRoomId, storage);
  const existingIndex = messages.findIndex((item) => item.clientMessageId === normalized.clientMessageId);
  const nextMessages =
    existingIndex >= 0
      ? messages.map((item, index) => (index === existingIndex ? normalized : item))
      : [...messages, normalized];

  writePendingChatMessages(chatRoomId, nextMessages, storage);
  return normalized;
};

const removePendingChatMessage = (chatRoomId, clientMessageId, storage) => {
  if (!clientMessageId) {
    return readPendingChatMessages(chatRoomId, storage).length;
  }

  const messages = readPendingChatMessages(chatRoomId, storage).filter((message) => message.clientMessageId !== clientMessageId);
  writePendingChatMessages(chatRoomId, messages, storage);
  return messages.length;
};

export const queuePendingChatMessage = (chatRoomId, contentOrMessage, storage, options = {}) => {
  const message =
    typeof contentOrMessage === 'object' && contentOrMessage !== null
      ? createPendingChatMessage({ ...contentOrMessage, chatRoomId, ...options })
      : createPendingChatMessage({ chatRoomId, content: contentOrMessage, ...options });

  return upsertPendingChatMessage(chatRoomId, message, storage);
};

export const buildChatPublishPayload = (message) => ({
  clientMessageId: message.clientMessageId,
  content: message.content,
  messageType: message.messageType || 'TEXT'
});

export const publishChatMessage = (client, chatRoomId, message) => {
  if (!client?.connected) {
    return {
      status: CHAT_DELIVERY_STATUS.QUEUED,
      errorMessage: 'Chat socket is not connected.'
    };
  }

  try {
    client.publish({
      destination: CHAT_DESTINATIONS.publish(chatRoomId),
      body: JSON.stringify(buildChatPublishPayload(message))
    });

    return { status: CHAT_DELIVERY_STATUS.PUBLISHED, message: null, transient: true };
  } catch (error) {
    return {
      status: CHAT_DELIVERY_STATUS.FAILED,
      errorMessage: getErrorMessage(error)
    };
  }
};

const markMessageForResult = (message, result) => {
  if (result.status === CHAT_DELIVERY_STATUS.FAILED) {
    return normalizePendingMessage({
      ...message,
      deliveryStatus: CHAT_DELIVERY_STATUS.FAILED,
      errorMessage: result.errorMessage
    });
  }

  return normalizePendingMessage({
    ...message,
    deliveryStatus: CHAT_DELIVERY_STATUS.PENDING,
    errorMessage: null
  });
};

export const retryPendingChatMessage = (chatRoomId, message, client, storage) => {
  const retryMessage = createPendingChatMessage({
    ...message,
    chatRoomId,
    clientMessageId: message.clientMessageId || createClientMessageId(),
    deliveryStatus: CHAT_DELIVERY_STATUS.PENDING
  });
  const result = publishChatMessage(client, chatRoomId, retryMessage);
  const nextMessage = markMessageForResult(retryMessage, result);

  if (result.status === CHAT_DELIVERY_STATUS.PUBLISHED) {
    const pendingCount = removePendingChatMessage(chatRoomId, nextMessage.clientMessageId, storage);
    return {
      ...result,
      transient: true,
      message: null,
      removeClientMessageIds: nextMessage.clientMessageId ? [nextMessage.clientMessageId] : [],
      pendingCount
    };
  }

  const storedMessage = queuePendingChatMessage(chatRoomId, nextMessage, storage);
  return { ...result, message: storedMessage, pendingCount: readPendingChatMessages(chatRoomId, storage).length };
};

export const flushPendingChatMessages = (chatRoomId, publish, storage) => {
  const messages = readPendingChatMessages(chatRoomId, storage);
  const remaining = [];
  const failedMessages = [];
  const sentClientMessageIds = [];
  let sentCount = 0;
  let failedCount = 0;

  for (const message of messages) {
    try {
      publish(buildChatPublishPayload(message), message);
      sentCount += 1;
      if (message.clientMessageId) {
        sentClientMessageIds.push(message.clientMessageId);
      }
    } catch (error) {
      const failedMessage = normalizePendingMessage({
        ...message,
        deliveryStatus: CHAT_DELIVERY_STATUS.FAILED,
        errorMessage: getErrorMessage(error)
      });

      failedCount += 1;
      failedMessages.push(failedMessage);
      remaining.push(failedMessage);
    }
  }

  writePendingChatMessages(chatRoomId, remaining, storage);

  return { sentCount, failedCount, pendingCount: remaining.length, failedMessages, sentClientMessageIds };
};

const getBrowserOrigin = () => (typeof window === 'undefined' ? 'http://127.0.0.1:8080' : window.location.origin);

const resolveApiBase = (apiBase, origin = getBrowserOrigin()) => {
  const value = String(apiBase || '').trim();

  if (!value || value.startsWith('/')) {
    return new URL(value || '/', origin).toString();
  }

  return value;
};

export const getSocketUrl = (apiBase = import.meta.env?.VITE_API_BASE_URL, origin) => {
  const base = resolveApiBase(apiBase || 'http://127.0.0.1:8080', origin);
  return new URL(CHAT_DESTINATIONS.endpoint, base).toString().replace(/^http/, 'ws');
};

const getConnectHeaders = () => {
  const authorization = formatAuthorizationHeader(getUserToken());
  return authorization ? { Authorization: authorization } : {};
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
    readPendingChatMessages(chatRoomId).forEach((message) => messageHandlerRef.current?.(message));

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
        const flushResult = flushPendingChatMessages(chatRoomId, (payload) => {
          client.publish({
            destination: CHAT_DESTINATIONS.publish(chatRoomId),
            body: JSON.stringify(payload)
          });
        });

        flushResult.failedMessages.forEach((message) => messageHandlerRef.current?.(message));
        if (flushResult.sentClientMessageIds.length > 0) {
          messageHandlerRef.current?.({
            source: 'local-control',
            removeClientMessageIds: flushResult.sentClientMessageIds
          });
        }
        setState({
          connected: true,
          error: flushResult.failedCount > 0 ? 'Some queued messages could not be sent. Please retry them.' : null,
          pendingCount: flushResult.pendingCount
        });
      } catch (error) {
        setState({
          connected: true,
          error: getErrorMessage(error, 'Queued messages could not be sent.'),
          pendingCount: readPendingChatMessages(chatRoomId).length
        });
      }
    };

    client.onStompError = (frame) => {
      if (!disposed) {
        setState((current) => ({ ...current, connected: false, error: frame.headers?.message || 'Chat connection error' }));
      }
    };

    client.onWebSocketError = () => {
      if (!disposed) {
        setState((current) => ({ ...current, connected: false, error: 'Realtime connection was lost.' }));
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
      const message = createPendingChatMessage({ chatRoomId, content });

      if (!chatRoomId) {
        return {
          queued: false,
          failed: true,
          message: { ...message, deliveryStatus: CHAT_DELIVERY_STATUS.FAILED, failed: true },
          pendingCount: 0,
          errorMessage: 'Chat room is not ready.'
        };
      }

      const result = publishChatMessage(client, chatRoomId, message);

      if (result.status === CHAT_DELIVERY_STATUS.PUBLISHED) {
        return {
          queued: false,
          failed: false,
          transient: true,
          message: null,
          pendingCount: readPendingChatMessages(chatRoomId).length
        };
      }

      const storedMessage = queuePendingChatMessage(chatRoomId, markMessageForResult(message, result));
      const pendingCount = readPendingChatMessages(chatRoomId).length;
      const isQueued = result.status === CHAT_DELIVERY_STATUS.QUEUED;

      setState((current) => ({
        ...current,
        connected: isQueued ? false : current.connected,
        error: isQueued ? 'Chat is offline. The message will be retried when the connection returns.' : result.errorMessage,
        pendingCount
      }));

      return {
        queued: isQueued,
        failed: result.status === CHAT_DELIVERY_STATUS.FAILED,
        message: storedMessage,
        pendingCount,
        errorMessage: result.errorMessage
      };
    },
    [chatRoomId]
  );

  const retryMessage = useCallback(
    (message) => {
      const client = clientRef.current;
      const result = retryPendingChatMessage(chatRoomId, message, client);

      setState((current) => ({
        ...current,
        connected: result.status === CHAT_DELIVERY_STATUS.QUEUED ? false : current.connected,
        error:
          result.status === CHAT_DELIVERY_STATUS.PUBLISHED
            ? null
            : result.errorMessage || 'Message could not be resent. Please try again.',
        pendingCount: result.pendingCount
      }));

      return {
        queued: result.status === CHAT_DELIVERY_STATUS.QUEUED,
        failed: result.status === CHAT_DELIVERY_STATUS.FAILED,
        transient: result.status === CHAT_DELIVERY_STATUS.PUBLISHED,
        message: result.message,
        removeClientMessageIds: result.removeClientMessageIds || [],
        pendingCount: result.pendingCount,
        errorMessage: result.errorMessage
      };
    },
    [chatRoomId]
  );

  return useMemo(() => ({ ...state, sendMessage, retryMessage }), [state, sendMessage, retryMessage]);
}
