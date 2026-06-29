import { useState } from 'react';
import { Alert, Button, Card, Form } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { getMyRooms, openChatRoom } from '../api/chatApi.js';
import { PageHeader, useApiResource } from './pageUtils.jsx';

export default function ChatRoomsPage() {
  const navigate = useNavigate();
  const [productId, setProductId] = useState('');
  const [actionError, setActionError] = useState('');
  const [opening, setOpening] = useState(false);
  const { data: rooms = [], error, loading, reload } = useApiResource(() => getMyRooms(), []);

  const handleOpenRoom = async (event) => {
    event.preventDefault();
    setActionError('');
    setOpening(true);

    try {
      const room = await openChatRoom(productId);
      navigate(`/chat/${room.chatRoomId}`);
    } catch (err) {
      setActionError(err.message);
    } finally {
      setOpening(false);
    }
  };

  return (
    <section>
      <PageHeader title="채팅" eyebrow="메시지" />
      <div className="toolbar-panel mb-3">
        {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
        <Form className="profile-form-row" onSubmit={handleOpenRoom}>
          <Form.Control
            type="number"
            min="1"
            value={productId}
            onChange={(event) => setProductId(event.target.value)}
            placeholder="상품 ID로 채팅 열기"
            aria-label="상품 ID"
            required
          />
          <Button type="submit" disabled={opening}>
            {opening ? '여는 중' : '채팅 열기'}
          </Button>
        </Form>
      </div>

      {loading ? <LoadingState label="채팅방 불러오는 중" /> : null}
      {error ? <ErrorState title="채팅방을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && rooms.length === 0 ? <EmptyState title="채팅방이 없어요" /> : null}
      {!loading && !error && rooms.length > 0 ? (
        <div className="stack-list">
          {rooms.map((room) => (
            <Card key={room.chatRoomId} className="list-card">
              <Card.Body>
                <div className="list-card-row">
                  <div>
                    <Card.Title as="h2">채팅방 #{room.chatRoomId}</Card.Title>
                    <p className="text-muted mb-0">상품 #{room.productId}</p>
                  </div>
                  <StatusBadge status={room.status} />
                </div>
                <dl className="compact-list compact-list-inline mt-3">
                  <div>
                    <dt>판매자</dt>
                    <dd>{room.sellerId}</dd>
                  </div>
                  <div>
                    <dt>구매자</dt>
                    <dd>{room.buyerId}</dd>
                  </div>
                  <div>
                    <dt>상태</dt>
                    <dd>{room.status || '-'}</dd>
                  </div>
                  <div>
                    <dt>입장</dt>
                    <dd>
                      <Link to={`/chat/${room.chatRoomId}`}>열기</Link>
                    </dd>
                  </div>
                </dl>
              </Card.Body>
            </Card>
          ))}
        </div>
      ) : null}
    </section>
  );
}
