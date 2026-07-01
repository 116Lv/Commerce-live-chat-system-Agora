import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Badge, Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { ImagePlus, RotateCcw, Send } from 'lucide-react';
import { getMessages, getMyRooms, markRoomRead, uploadChatImage } from '../api/chatApi.js';
import { getUserToken } from '../auth/tokenStorage.js';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import NegoPanel from '../features/nego/NegoPanel.jsx';
import useChatSocket, { CHAT_DELIVERY_STATUS } from '../features/chat/useChatSocket.js';
import { PageHeader, formatDateTime, statusText, useApiResource } from './pageUtils.jsx';
import { normalizeProductImageUrl } from './productFormUtils.js';

const getMessageKey = (message, index = 0) =>
  message.clientMessageId ?? message.messageId ?? `local-${index}-${message.createdAt ?? ''}-${message.content ?? ''}`;

const getMessageTime = (message) => {
  const timestamp = Date.parse(message.createdAt || '');
  return Number.isNaN(timestamp) ? 0 : timestamp;
};

const isLocalDeliveryMessage = (message) =>
  message.source === 'local' ||
  message.deliveryStatus === CHAT_DELIVERY_STATUS.PENDING ||
  message.deliveryStatus === CHAT_DELIVERY_STATUS.FAILED;

const removeLocalMessagesByClientId = (messages, clientMessageIds = []) => {
  const ids = new Set(clientMessageIds.filter(Boolean));

  if (ids.size === 0) {
    return messages;
  }

  return messages.filter((message) => !ids.has(message.clientMessageId));
};

const dedupeMessages = (messages) => {
  const byKey = new Map();

  messages.forEach((message, index) => {
    byKey.set(getMessageKey(message, index), message);
  });

  return Array.from(byKey.values()).sort((a, b) => {
    const timeDelta = getMessageTime(a) - getMessageTime(b);

    if (timeDelta !== 0) {
      return timeDelta;
    }

    return Number(a.messageId ?? 0) - Number(b.messageId ?? 0);
  });
};

const getCurrentUserIdFromToken = () => {
  try {
    const token = getUserToken()?.replace(/^Bearer\s+/i, '');
    const payload = JSON.parse(globalThis.atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const userId = payload?.userId ?? payload?.id ?? payload?.sub;

    return userId == null ? null : String(userId);
  } catch {
    return null;
  }
};

const isSystemMessage = (message) => String(message.messageType || '').toUpperCase() === 'SYSTEM';

const isOwnMessage = (message, currentUserId) => {
  if (typeof message.isMine === 'boolean') {
    return message.isMine;
  }

  if (typeof message.mine === 'boolean') {
    return message.mine;
  }

  if (typeof message.ownedByCurrentUser === 'boolean') {
    return message.ownedByCurrentUser;
  }

  return currentUserId != null && [message.senderId, message.userId].some((id) => id != null && String(id) === currentUserId);
};

const canReconcileServerEcho = (message, currentUserId) =>
  currentUserId != null && !isLocalDeliveryMessage(message) && isOwnMessage(message, currentUserId);

const isMatchingLocalEcho = (localMessage, serverMessage) =>
  isLocalDeliveryMessage(localMessage) &&
  String(localMessage.messageType || 'TEXT').toUpperCase() === String(serverMessage.messageType || 'TEXT').toUpperCase() &&
  String(localMessage.content ?? '') === String(serverMessage.content ?? '');

const reconcileServerEchoWithLocalMessages = (messages, serverMessage, currentUserId) => {
  if (!canReconcileServerEcho(serverMessage, currentUserId)) {
    return messages;
  }

  return messages.filter((message) => !isMatchingLocalEcho(message, serverMessage));
};

const isSafeChatImageUrl = (value) => {
  try {
    const url = new URL(value, window.location.origin);
    const safeProtocols = new Set(['http:', 'https:', 'blob:']);

    if (url.protocol === 'javascript:') {
      return false;
    }

    return safeProtocols.has(url.protocol);
  } catch {
    return false;
  }
};

const getMessageClassName = (message, currentUserId) =>
  [
    'message-item',
    isSystemMessage(message) ? 'message-item-system' : '',
    isOwnMessage(message, currentUserId) ? 'message-item-own' : 'message-item-other',
    message.deliveryStatus === CHAT_DELIVERY_STATUS.PENDING ? 'message-item-pending' : '',
    message.deliveryStatus === CHAT_DELIVERY_STATUS.FAILED ? 'message-item-failed' : ''
  ]
    .filter(Boolean)
    .join(' ');

const formatProductPrice = (value) => {
  const amount = Number(value);

  if (!Number.isFinite(amount)) {
    return 'Price unavailable';
  }

  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0
  }).format(amount);
};

const getParticipantLine = (room) =>
  [
    room?.sellerNickname ? `판매자 ${room.sellerNickname}` : null,
    room?.buyerNickname ? `구매자 ${room.buyerNickname}` : null
  ]
    .filter(Boolean)
    .join(' · ');

function ChatRoomProductCard({ room }) {
  if (!room) {
    return null;
  }

  const thumbnailUrl = normalizeProductImageUrl(room.productThumbnailUrl);

  return (
    <div className="chat-room-product-card">
      <div className="chat-room-product-thumb" aria-hidden="true">
        {thumbnailUrl ? (
          <img src={thumbnailUrl} alt="" loading="lazy" />
        ) : (
          <span>{String(room.productTitle || '상품').slice(0, 1)}</span>
        )}
      </div>
      <div className="chat-room-product-card-main">
        <div>
          <h2>{room.productTitle || `Product #${room.productId}`}</h2>
          <p>{getParticipantLine(room) || 'Participants unavailable'}</p>
        </div>
        <div className="chat-room-product-card-meta">
          <strong>{formatProductPrice(room.productPrice)}</strong>
          {room.productStatus ? <Badge bg="light" text="dark">{statusText(room.productStatus)}</Badge> : null}
        </div>
      </div>
    </div>
  );
}

function ChatImageMessage({ message }) {
  const canPreview = isSafeChatImageUrl(message.content);

  if (!canPreview) {
    return <span className="chat-image-fallback">Image link unavailable</span>;
  }

  return (
    <div className="chat-image-message">
      <img className="chat-image-preview" src={message.content} alt="Chat attachment" loading="lazy" />
      <a href={message.content} target="_blank" rel="noreferrer">
        Open image
      </a>
    </div>
  );
}

function DeliveryStatus({ message, onRetry }) {
  if (message.deliveryStatus === CHAT_DELIVERY_STATUS.PENDING) {
    return <span className="message-delivery-status">Sending...</span>;
  }

  if (message.deliveryStatus === CHAT_DELIVERY_STATUS.FAILED) {
    return (
      <span className="message-delivery-status message-delivery-status-failed">
        Failed
        <Button type="button" variant="link" size="sm" onClick={() => onRetry(message)}>
          <RotateCcw size={14} aria-hidden="true" />
          Retry
        </Button>
      </span>
    );
  }

  return null;
}

export default function ChatRoomPage() {
  const { chatRoomId } = useParams();
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [uploading, setUploading] = useState(false);
  const messageListEndRef = useRef(null);
  const { data, error, loading, reload } = useApiResource(() => getMessages(chatRoomId, { size: 100 }), [chatRoomId]);
  const { data: roomList = [] } = useApiResource(() => getMyRooms(), [chatRoomId]);
  const currentUserId = useMemo(() => getCurrentUserIdFromToken(), []);
  const chatRoomMetadata = useMemo(
    () => (Array.isArray(roomList) ? roomList.find((room) => String(room.chatRoomId) === String(chatRoomId)) : null),
    [chatRoomId, roomList]
  );

  useEffect(() => {
    if (Array.isArray(data)) {
      setMessages((current) => {
        const localMessages = current.filter(
          (message) => isLocalDeliveryMessage(message) && String(message.chatRoomId) === String(chatRoomId)
        );
        const unreconciledLocalMessages = data.reduce(
          (remainingLocalMessages, serverMessage) =>
            reconcileServerEchoWithLocalMessages(remainingLocalMessages, serverMessage, currentUserId),
          localMessages
        );

        return dedupeMessages([...data, ...unreconciledLocalMessages]);
      });
      markRoomRead(chatRoomId).catch(() => {});
    }
  }, [chatRoomId, currentUserId, data]);

  const appendMessage = useCallback(
    (message) => {
      if (message?.removeClientMessageIds) {
        setMessages((current) => removeLocalMessagesByClientId(current, message.removeClientMessageIds));
        return;
      }

      setMessages((current) => {
        const reconciled = reconcileServerEchoWithLocalMessages(current, message, currentUserId);

        return dedupeMessages([...reconciled, message]);
      });
    },
    [currentUserId]
  );

  const socket = useChatSocket(chatRoomId, appendMessage);
  const renderedMessages = useMemo(() => dedupeMessages(messages), [messages]);
  const lastRenderedMessageKey = renderedMessages.length > 0 ? getMessageKey(renderedMessages[renderedMessages.length - 1]) : '';

  useEffect(() => {
    if (!lastRenderedMessageKey) {
      return;
    }

    const animationFrameId = window.requestAnimationFrame(() => {
      messageListEndRef.current?.scrollIntoView({ block: 'end', behavior: 'smooth' });
    });

    return () => window.cancelAnimationFrame(animationFrameId);
  }, [lastRenderedMessageKey]);

  const handleSend = (event) => {
    event.preventDefault();
    setActionError('');
    setActionMessage('');

    try {
      const content = draft.trim();
      const result = socket.sendMessage(content);

      if (result.message && !result.transient) {
        appendMessage(result.message);
      }

      if (result.queued) {
        setActionMessage('Message queued. It will send when the chat reconnects.');
      } else if (result.failed) {
        setActionError(result.errorMessage || 'Message could not be sent. Please retry.');
      }

      setDraft('');
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleRetryMessage = (message) => {
    setActionError('');
    setActionMessage('');

    try {
      const result = socket.retryMessage(message);

      if (result.removeClientMessageIds?.length > 0) {
        appendMessage({ source: 'local-control', removeClientMessageIds: result.removeClientMessageIds });
      }

      if (result.message && !result.transient) {
        appendMessage(result.message);
      }

      if (result.queued) {
        setActionMessage('Message queued. It will send when the chat reconnects.');
      } else if (result.failed) {
        setActionError(result.errorMessage || 'Message could not be resent. Please try again.');
      }
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleImage = async (event) => {
    const image = event.target.files?.[0];

    if (!image) {
      return;
    }

    setActionError('');
    setActionMessage('');
    setUploading(true);

    try {
      const response = await uploadChatImage(chatRoomId, image);
      appendMessage(response);
      setActionMessage('Image sent.');
    } catch (err) {
      setActionError(err.message);
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  return (
    <section>
      <PageHeader
        title={`Chat room #${chatRoomId}`}
        eyebrow="Messages"
        action={
          <Button as={Link} to="/chat" variant="outline-primary">
            Rooms
          </Button>
        }
      />
      {socket.error ? <Alert variant="warning">Realtime chat: {socket.error}</Alert> : null}
      {socket.pendingCount > 0 ? <Alert variant="info">Queued messages: {socket.pendingCount}</Alert> : null}
      {actionMessage ? <Alert variant="success">{actionMessage}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      <Row className="g-3">
        <Col xs={12} lg={8}>
          <ChatRoomProductCard room={chatRoomMetadata} />
          <div className="detail-panel chat-room-panel">
            {loading ? <LoadingState label="Loading messages" /> : null}
            {error ? <ErrorState title="Could not load messages" message={error.message} onRetry={reload} /> : null}
            {!loading && !error && renderedMessages.length === 0 ? <EmptyState title="No messages yet" /> : null}
            {!loading && !error && renderedMessages.length > 0 ? (
              <div className="message-list">
                {renderedMessages.map((message, index) => {
                  if (isSystemMessage(message)) {
                    return (
                      <article key={getMessageKey(message, index)} className={getMessageClassName(message, currentUserId)}>
                        <p>{message.content}</p>
                      </article>
                    );
                  }

                  return (
                    <article key={getMessageKey(message, index)} className={getMessageClassName(message, currentUserId)}>
                      <div className="message-meta">
                        <strong>{message.senderNickname || `User ${message.senderId || '-'}`}</strong>
                        <span>{formatDateTime(message.createdAt)}</span>
                      </div>
                      {message.messageType === 'IMAGE' ? <ChatImageMessage message={message} /> : <p>{message.content}</p>}
                      <DeliveryStatus message={message} onRetry={handleRetryMessage} />
                    </article>
                  );
                })}
                <div ref={messageListEndRef} aria-hidden="true" />
              </div>
            ) : null}
            <Form className="chat-compose" onSubmit={handleSend}>
              <Form.Control
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                placeholder={socket.connected ? 'Type a message' : 'Will send after reconnect'}
                maxLength={1000}
              />
              <Button type="submit" disabled={!draft.trim()} aria-label="Send message">
                <Send size={17} aria-hidden="true" />
              </Button>
              <Button as="label" variant="outline-primary" aria-label="Send image" disabled={uploading}>
                <ImagePlus size={17} aria-hidden="true" />
                <Form.Control type="file" accept="image/*" className="visually-hidden" onChange={handleImage} />
              </Button>
            </Form>
          </div>
        </Col>
        <Col xs={12} lg={4}>
          <NegoPanel chatRoomId={chatRoomId} />
        </Col>
      </Row>
    </section>
  );
}
