import { useState } from 'react';
import { Alert, Button, Card, Col, Row } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getCouponEvents, issueCoupon } from '../api/couponApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { PageHeader, formatDateTime, getPageContent, statusText, useApiResource } from './pageUtils.jsx';

export default function CouponEventsPage() {
  const { data, error, loading, reload } = useApiResource(() => getCouponEvents(), []);
  const navigate = useNavigate();
  const location = useLocation();
  const { isUserAuthenticated } = useAuth();
  const [message, setMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [issuingId, setIssuingId] = useState(null);
  const events = getPageContent(data);

  const handleIssue = async (eventId) => {
    if (!isUserAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    setIssuingId(eventId);
    setMessage('');
    setActionError('');

    try {
      await issueCoupon(eventId);
      setMessage('쿠폰을 발급했어요.');
      await reload();
    } catch (err) {
      setActionError(err.message);
    } finally {
      setIssuingId(null);
    }
  };

  return (
    <section>
      <PageHeader title="쿠폰 이벤트" eyebrow="혜택" />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      {loading ? <LoadingState label="이벤트 불러오는 중" /> : null}
      {error ? <ErrorState title="이벤트를 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && events.length === 0 ? <EmptyState title="진행 중인 이벤트가 없어요" /> : null}
      {!loading && !error && events.length > 0 ? (
        <Row className="g-3">
          {events.map((event) => (
            <Col key={event.eventId} xs={12} md={6} xl={4}>
              <Card className="list-card h-100">
                <Card.Body>
                  <div className="d-flex justify-content-between gap-2 align-items-start mb-2">
                    <Card.Title as="h2">{event.name}</Card.Title>
                    <span className="text-muted small">{statusText(event.status)}</span>
                  </div>
                  <p className="coupon-amount">{Number(event.discountAmount || 0).toLocaleString('ko-KR')}원</p>
                  <dl className="compact-list">
                    <div>
                      <dt>최소 주문</dt>
                      <dd>{Number(event.minOrderAmount || 0).toLocaleString('ko-KR')}원</dd>
                    </div>
                    <div>
                      <dt>수량</dt>
                      <dd>
                        {event.issuedQuantity}/{event.totalQuantity}
                      </dd>
                    </div>
                    <div>
                      <dt>기간</dt>
                      <dd>{formatDateTime(event.endAt)}</dd>
                    </div>
                  </dl>
                  <Button onClick={() => handleIssue(event.eventId)} disabled={issuingId === event.eventId}>
                    {issuingId === event.eventId ? '발급 중' : '발급'}
                  </Button>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
