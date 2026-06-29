import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { getUserToken } from '../../auth/tokenStorage.js';

export const CHAT_DESTINATIONS = {
  endpoint: '/ws',
  subscribe: (chatRoomId) => `/sub/chat/${chatRoomId}`,
  publish: (chatRoomId) => `/pub/chat/${chatRoomId}/messages`
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
  const [state, setState] = useState({ connected: false, error: null });

  useEffect(() => {
    messageHandlerRef.current = onMessage;
  }, [onMessage]);

  useEffect(() => {
    if (!chatRoomId) {
      return undefined;
    }

    let disposed = false;
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

      setState({ connected: true, error: null });
      client.subscribe(CHAT_DESTINATIONS.subscribe(chatRoomId), (frame) => {
        try {
          messageHandlerRef.current?.(JSON.parse(frame.body));
        } catch {
          messageHandlerRef.current?.({ content: frame.body });
        }
      });
    };

    client.onStompError = (frame) => {
      if (!disposed) {
        setState({ connected: false, error: frame.headers?.message || '채팅 연결 오류' });
      }
    };

    client.onWebSocketError = () => {
      if (!disposed) {
        setState({ connected: false, error: '실시간 연결이 끊겼어요.' });
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
        throw new Error('실시간 채팅에 연결되지 않았어요.');
      }

      client.publish({
        destination: CHAT_DESTINATIONS.publish(chatRoomId),
        body: JSON.stringify({ content })
      });
    },
    [chatRoomId]
  );

  return useMemo(() => ({ ...state, sendMessage }), [state, sendMessage]);
}
