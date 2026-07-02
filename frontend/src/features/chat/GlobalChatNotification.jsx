import { useEffect, useMemo, useState } from 'react';
import { MessageCircle, X } from 'lucide-react';
import { useLocation, useNavigate } from 'react-router-dom';
import useChatNotifications, { getActiveChatRoomId, getCurrentUserIdFromToken } from './useChatNotifications.js';

const AUTO_DISMISS_MS = 8000;

export default function GlobalChatNotification({ enabled, userToken }) {
  const location = useLocation();
  const navigate = useNavigate();
  const [notification, setNotification] = useState(null);
  const activeChatRoomId = getActiveChatRoomId(location.pathname);
  const currentUserId = useMemo(() => getCurrentUserIdFromToken(userToken), [userToken]);

  useChatNotifications({
    enabled,
    activeChatRoomId,
    currentUserId,
    onNotification: setNotification
  });

  useEffect(() => {
    if (!notification) {
      return undefined;
    }

    const timerId = window.setTimeout(() => setNotification(null), AUTO_DISMISS_MS);
    return () => window.clearTimeout(timerId);
  }, [notification]);

  if (!enabled || !notification) {
    return null;
  }

  const openChatRoom = () => {
    const targetChatRoomId = notification.chatRoomId;
    setNotification(null);
    navigate(`/chat/${targetChatRoomId}`);
  };

  return (
    <div className="chat-notification-shell" role="status" aria-live="polite">
      <button type="button" className="chat-notification-banner" onClick={openChatRoom}>
        <span className="chat-notification-icon" aria-hidden="true">
          <MessageCircle size={18} />
        </span>
        <span className="chat-notification-content">
          <span className="chat-notification-title">
            <strong>{notification.sender}</strong>
            <span>{notification.context}</span>
          </span>
          <span className="chat-notification-preview">{notification.preview}</span>
        </span>
      </button>
      <button
        type="button"
        className="chat-notification-dismiss"
        aria-label="Dismiss chat notification"
        onClick={() => setNotification(null)}
      >
        <X size={17} aria-hidden="true" />
      </button>
    </div>
  );
}
