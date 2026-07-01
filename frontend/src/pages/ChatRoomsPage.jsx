import { Badge, Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { getMyRooms } from '../api/chatApi.js';
import { PageHeader, formatDateTime, statusText, useApiResource } from './pageUtils.jsx';
import { normalizeProductImageUrl } from './productFormUtils.js';

const formatPrice = (value) => {
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

const getLastMessageText = (room) => {
  if (!room.lastMessagePreview) {
    return 'No messages yet';
  }

  if (String(room.lastMessageType || '').toUpperCase() === 'IMAGE') {
    return 'Image message';
  }

  if (String(room.lastMessageType || '').toUpperCase() === 'SYSTEM') {
    return room.lastMessagePreview;
  }

  return room.lastMessagePreview;
};

const getParticipantLine = (room) =>
  [room.sellerNickname ? `Seller ${room.sellerNickname}` : null, room.buyerNickname ? `Buyer ${room.buyerNickname}` : null]
    .filter(Boolean)
    .join(' · ');

export default function ChatRoomsPage() {
  const { data: rooms = [], error, loading, reload } = useApiResource(() => getMyRooms(), []);

  return (
    <section>
      <PageHeader title="채팅" eyebrow="메시지" />

      {loading ? <LoadingState label="채팅방 불러오는 중" /> : null}
      {error ? <ErrorState title="채팅방을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && rooms.length === 0 ? <EmptyState title="채팅방이 없어요" /> : null}
      {!loading && !error && rooms.length > 0 ? (
        <div className="stack-list">
          {rooms.map((room) => (
            <Card key={room.chatRoomId} className="list-card chat-room-list-card">
              <Card.Body>
                <div className="chat-room-row">
                  <div className="chat-room-product-thumb" aria-hidden="true">
                    {normalizeProductImageUrl(room.productThumbnailUrl) ? (
                      <img src={normalizeProductImageUrl(room.productThumbnailUrl)} alt="" loading="lazy" />
                    ) : (
                      <span>{String(room.productTitle || '상품').slice(0, 1)}</span>
                    )}
                  </div>
                  <div className="chat-room-row-main">
                    <div className="list-card-row">
                      <div>
                        <Card.Title as="h2">{room.productTitle || `상품 #${room.productId}`}</Card.Title>
                        <p className="chat-room-participants">{getParticipantLine(room) || 'Participants unavailable'}</p>
                      </div>
                      <div className="chat-room-row-status">
                        <StatusBadge status={room.productStatus || room.status} />
                        {room.status ? <Badge bg="light" text="dark">{statusText(room.status)}</Badge> : null}
                      </div>
                    </div>
                    <div className="chat-room-message-row">
                      <p className="chat-room-last-message">
                        <span className="chat-room-last-message-type">{room.lastMessageType || 'TEXT'}</span>
                        {getLastMessageText(room)}
                      </p>
                      <span className="chat-room-last-time">{formatDateTime(room.lastMessageCreatedAt)}</span>
                    </div>
                    <div className="chat-room-row-footer">
                      <span className="list-price">{formatPrice(room.productPrice)}</span>
                      {Number(room.unreadCount) > 0 ? (
                        <Badge bg="danger" className="chat-room-unread-badge">
                          {room.unreadCount}
                        </Badge>
                      ) : (
                        <span className="chat-room-read-state">Read</span>
                      )}
                      <Button as={Link} to={`/chat/${room.chatRoomId}`} size="sm" variant="outline-primary">
                        Open
                      </Button>
                    </div>
                  </div>
                </div>
              </Card.Body>
            </Card>
          ))}
        </div>
      ) : null}
    </section>
  );
}
