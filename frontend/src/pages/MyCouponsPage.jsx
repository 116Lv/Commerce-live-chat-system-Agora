import { Card, Col, Row } from 'react-bootstrap';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyCoupons } from '../api/couponApi.js';
import { PageHeader, formatDateTime, getPageContent, statusText, useApiResource } from './pageUtils.jsx';

export default function MyCouponsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyCoupons(), []);
  const coupons = getPageContent(data);

  return (
    <section>
      <PageHeader title="내 쿠폰" eyebrow="마이페이지" />
      {loading ? <LoadingState label="쿠폰 불러오는 중" /> : null}
      {error ? <ErrorState title="쿠폰을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && coupons.length === 0 ? <EmptyState title="보유 쿠폰이 없어요" /> : null}
      {!loading && !error && coupons.length > 0 ? (
        <Row className="g-3">
          {coupons.map((coupon) => (
            <Col key={coupon.couponId} xs={12} md={6} xl={4}>
              <Card className="list-card h-100">
                <Card.Body>
                  <Card.Title as="h2">{coupon.eventName}</Card.Title>
                  <p className="coupon-amount">{Number(coupon.discountAmount || 0).toLocaleString('ko-KR')}원</p>
                  <dl className="compact-list">
                    <div>
                      <dt>상태</dt>
                      <dd>{statusText(coupon.status)}</dd>
                    </div>
                    <div>
                      <dt>최소 주문</dt>
                      <dd>{Number(coupon.minOrderAmount || 0).toLocaleString('ko-KR')}원</dd>
                    </div>
                    <div>
                      <dt>만료</dt>
                      <dd>{formatDateTime(coupon.expiresAt)}</dd>
                    </div>
                  </dl>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
