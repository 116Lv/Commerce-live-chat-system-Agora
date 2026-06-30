import { useMemo, useState } from 'react';
import { Badge, Button, ButtonGroup, Card, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyCoupons } from '../api/couponApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';
import {
  filterCoupons,
  formatCouponMoney,
  formatCouponPeriod,
  getCouponStatusMeta,
  getDiscountConditionText,
  getExpiryPriority,
  sortMyCoupons,
  toCouponFilter
} from './couponUtils.js';

const FILTERS = [
  { value: 'all', label: '전체' },
  { value: 'usable', label: '사용 가능' },
  { value: 'used', label: '사용 완료' },
  { value: 'expired', label: '만료' }
];

function CouponStatusBadge({ coupon }) {
  const status = getCouponStatusMeta(coupon);
  return <Badge bg={status.variant}>{status.label}</Badge>;
}

export default function MyCouponsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyCoupons(), []);
  const [activeFilter, setActiveFilter] = useState('all');
  const coupons = getPageContent(data);
  const sortedCoupons = useMemo(() => sortMyCoupons(coupons), [coupons]);
  const visibleCoupons = useMemo(() => filterCoupons(sortedCoupons, activeFilter), [activeFilter, sortedCoupons]);

  return (
    <section>
      <PageHeader title="내 쿠폰" eyebrow="마이페이지" />
      {loading ? <LoadingState label="쿠폰 불러오는 중" /> : null}
      {error ? <ErrorState title="쿠폰을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && coupons.length === 0 ? <EmptyState title="보유 쿠폰이 없어요" /> : null}
      {!loading && !error && coupons.length > 0 ? (
        <>
          <ButtonGroup className="coupon-filter-tabs mb-3" aria-label="쿠폰 상태 필터">
            {FILTERS.map((filter) => (
              <Button
                key={filter.value}
                type="button"
                variant={activeFilter === filter.value ? 'primary' : 'outline-primary'}
                aria-pressed={activeFilter === filter.value}
                onClick={() => setActiveFilter(filter.value)}
              >
                {filter.label}
              </Button>
            ))}
          </ButtonGroup>
          {visibleCoupons.length === 0 ? <EmptyState title="조건에 맞는 쿠폰이 없어요" /> : null}
          {visibleCoupons.length > 0 ? (
            <Row className="g-3">
              {visibleCoupons.map((coupon) => {
                const filter = toCouponFilter(coupon);
                const isUsable = filter === 'usable';
                const isExpiringSoon = getExpiryPriority(coupon) === 'soon';

                return (
                  <Col key={coupon.couponId} xs={12} md={6} xl={4}>
                    <Card className={`coupon-ticket-card h-100 ${isExpiringSoon ? 'coupon-expiring-soon' : ''}`}>
                      <Card.Body>
                        <div className="coupon-ticket-header">
                          <Card.Title as="h2">{coupon.eventName}</Card.Title>
                          <CouponStatusBadge coupon={coupon} />
                        </div>
                        <p className="coupon-amount">{formatCouponMoney(coupon.discountAmount)}</p>
                        <p className="coupon-condition">{getDiscountConditionText(coupon)}</p>
                        <dl className="compact-list coupon-ticket-meta">
                          <div>
                            <dt>만료</dt>
                            <dd>{formatCouponPeriod({ expiresAt: coupon.expiresAt })}</dd>
                          </div>
                          <div>
                            <dt>상태</dt>
                            <dd>{getCouponStatusMeta(coupon).label}</dd>
                          </div>
                        </dl>
                        {isExpiringSoon ? <p className="coupon-expiry-note">3일 안에 만료돼요</p> : null}
                        {isUsable ? (
                          <Button as={Link} to="/products" className="w-100">
                            상품 보러가기
                          </Button>
                        ) : null}
                      </Card.Body>
                    </Card>
                  </Col>
                );
              })}
            </Row>
          ) : null}
        </>
      ) : null}
    </section>
  );
}
