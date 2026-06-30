import assert from 'node:assert/strict';
import { describe, test } from 'node:test';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

import {
  buildIssueButtonState,
  formatCouponMoney,
  formatCouponPeriod,
  getCouponStatusMeta,
  getDiscountConditionText,
  getExpiryPriority,
  getIssueProgress,
  getRemainingQuantityText,
  sortMyCoupons,
  toCouponFilter
} from './couponUtils.js';

const __dirname = dirname(fileURLToPath(import.meta.url));
const readSource = (relativePath) => readFileSync(resolve(__dirname, relativePath), 'utf8');

describe('coupon formatting utilities', () => {
  test('formats money, conditions, period, remaining quantity, and issue progress', () => {
    assert.equal(formatCouponMoney(12000), '12,000원');
    assert.equal(getDiscountConditionText({ discountAmount: 5000, minOrderAmount: 30000 }), '30,000원 이상 주문 시 5,000원 할인');
    assert.equal(getDiscountConditionText({ discountAmount: 5000, minOrderAmount: 0 }), '주문 금액 제한 없이 5,000원 할인');
    assert.match(formatCouponPeriod({ startAt: '2026-06-27T00:00:00+09:00', endAt: '2026-06-30T00:00:00+09:00' }, new Date('2026-06-29T00:00:00+09:00')), /내일까지/);
    assert.equal(getRemainingQuantityText({ issuedQuantity: 3, totalQuantity: 10 }), '7장 남음');
    assert.deepEqual(getIssueProgress({ issuedQuantity: 3, totalQuantity: 10 }), { issued: 3, total: 10, rate: 30 });
  });

  test('maps status labels, badge variants, filters, expiry priority, and issue buttons defensively', () => {
    assert.deepEqual(getCouponStatusMeta({ status: 'ACTIVE' }), { label: '발급 가능', variant: 'success' });
    assert.deepEqual(getCouponStatusMeta({ status: 'USED' }), { label: '사용 완료', variant: 'secondary' });
    assert.equal(toCouponFilter({ status: 'EXPIRED' }), 'expired');
    assert.equal(getExpiryPriority({ status: 'ISSUED', expiresAt: '2026-07-01T00:00:00+09:00' }, new Date('2026-06-29T00:00:00+09:00')), 'soon');
    assert.deepEqual(buildIssueButtonState({ issuedByMe: true }), { disabled: true, label: '이미 발급됨' });
    assert.deepEqual(buildIssueButtonState({ status: 'ENDED' }), { disabled: true, label: '종료됨' });
    assert.deepEqual(buildIssueButtonState({ issuedQuantity: 10, totalQuantity: 10 }), { disabled: true, label: '소진됨' });
  });

  test('sorts my coupons usable first, then used, then expired', () => {
    const now = new Date('2026-06-29T00:00:00+09:00');
    const sorted = sortMyCoupons([
      { couponId: 3, status: 'EXPIRED', expiresAt: '2026-06-28T00:00:00+09:00' },
      { couponId: 1, status: 'ISSUED', expiresAt: '2026-06-30T00:00:00+09:00' },
      { couponId: 2, status: 'USED', expiresAt: '2026-07-01T00:00:00+09:00' }
    ], now);

    assert.deepEqual(sorted.map((coupon) => coupon.couponId), [1, 2, 3]);
  });

  test('treats coupons expired earlier today as ended before day labels', () => {
    const now = new Date('2026-06-29T10:00:00+09:00');
    const coupon = { status: 'ISSUED', expiresAt: '2026-06-29T09:00:00+09:00' };

    assert.equal(toCouponFilter(coupon, now), 'expired');
    assert.equal(formatCouponPeriod(coupon, now), '기간 종료');
  });

  test('disables issue action for explicit sold out status without quantity counters', () => {
    assert.deepEqual(buildIssueButtonState({ status: 'SOLD_OUT' }), { disabled: true, label: '소진됨' });
  });
});

describe('coupon page UX source', () => {
  test('CouponEventsPage renders ticket cards with shared coupon helpers and issue state', () => {
    const source = readSource('./CouponEventsPage.jsx');

    assert.match(source, /coupon-ticket-card/);
    assert.match(source, /getDiscountConditionText/);
    assert.match(source, /formatCouponPeriod/);
    assert.match(source, /getRemainingQuantityText/);
    assert.match(source, /getIssueProgress/);
    assert.match(source, /buildIssueButtonState/);
    assert.match(source, /coupon-progress/);
    assert.match(source, /CouponStatusBadge/);
  });

  test('MyCouponsPage renders filters, sorted coupons, CTA, and expiring-soon marker', () => {
    const source = readSource('./MyCouponsPage.jsx');

    assert.match(source, /sortMyCoupons/);
    assert.match(source, /activeFilter/);
    assert.match(source, /coupon-filter-tabs/);
    assert.match(source, /filterCoupons/);
    assert.match(source, /toCouponFilter/);
    assert.match(source, /\/products/);
    assert.match(source, /coupon-expiring-soon/);
    assert.match(source, /CouponStatusBadge/);
    assert.match(source, /aria-pressed=\{activeFilter === filter\.value\}/);
  });
});
