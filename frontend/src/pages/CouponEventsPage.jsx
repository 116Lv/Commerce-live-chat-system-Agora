import { useState } from 'react';
import { Alert, Badge, Button, Card, Col, ProgressBar, Row } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getCouponEvents, issueCoupon } from '../api/couponApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';
import {
  buildIssueButtonState,
  formatCouponMoney,
  formatCouponPeriod,
  getCouponStatusMeta,
  getDiscountConditionText,
  getIssueProgress,
  getRemainingQuantityText
} from './couponUtils.js';

function CouponStatusBadge({ coupon }) {
  const status = getCouponStatusMeta(coupon);
  return <Badge bg={status.variant}>{status.label}</Badge>;
}

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
      setMessage('쿠폰이 발급되었어요.');
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
          {events.map((event) => {
            const progress = getIssueProgress(event);
            const buttonState = buildIssueButtonState(event, { issuing: issuingId === event.eventId });

            return (
              <Col key={event.eventId} xs={12} md={6} xl={4}>
                <Card className="coupon-ticket-card h-100">
                  <Card.Body>
                    <div className="coupon-ticket-header">
                      <Card.Title as="h2">{event.name}</Card.Title>
                      <CouponStatusBadge coupon={event} />
                    </div>
                    <p className="coupon-amount">{formatCouponMoney(event.discountAmount)}</p>
                    <p className="coupon-condition">{getDiscountConditionText(event)}</p>
                    <dl className="compact-list coupon-ticket-meta">
                      <div>
                        <dt>기간</dt>
                        <dd>{formatCouponPeriod(event)}</dd>
                      </div>
                      <div>
                        <dt>잔여</dt>
                        <dd>{getRemainingQuantityText(event)}</dd>
                      </div>
                    </dl>
                    <div className="coupon-progress" aria-label="쿠폰 발급률">
                      <ProgressBar now={progress.rate} label={`${progress.rate}%`} visuallyHidden />
                      <span>
                        {progress.total > 0
                          ? `${progress.issued.toLocaleString('ko-KR')} / ${progress.total.toLocaleString('ko-KR')}장 발급`
                          : '발급 수량 제한 없음'}
                      </span>
                    </div>
                    <Button className="w-100" onClick={() => handleIssue(event.eventId)} disabled={buttonState.disabled}>
                      {buttonState.label}
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            );
          })}
        </Row>
      ) : null}
    </section>
  );
}
