import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { describe, test } from 'node:test';
import {
  buildAdminCouponCreatePayload,
  canAdminIssueCoupon,
  formatAdminCouponMoneyInput,
  formatAdminCouponStatus,
  formatAdminCouponType,
  formatIssueRate,
  getCouponTargetChipKey,
  parseAdminCouponNumber,
  parseUserIdTokens,
  validateAdminCouponForm
} from './adminPageUtils.js';

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf8');

describe('admin coupon helpers', () => {
  test('maps coupon raw enums to Korean admin labels', () => {
    assert.equal(formatAdminCouponType('FIRST_COME'), '선착순');
    assert.equal(formatAdminCouponType('NEW_SIGNUP'), '신규 가입');
    assert.equal(formatAdminCouponType('ADMIN_INDIVIDUAL'), '관리자 개별 발급');
    assert.equal(formatAdminCouponStatus('SOLD_OUT', 'Sold out'), '소진');
    assert.equal(formatAdminCouponStatus('ACTIVE', 'Active'), '진행중');
  });

  test('formats and parses comma number input while building numeric payloads', () => {
    assert.equal(formatAdminCouponMoneyInput('1200000'), '1,200,000');
    assert.equal(formatAdminCouponMoneyInput('1,200,000원'), '1,200,000');
    assert.equal(parseAdminCouponNumber('1,200,000원'), 1200000);

    assert.deepEqual(
      buildAdminCouponCreatePayload({
        type: 'ADMIN_INDIVIDUAL',
        name: ' 여름 쿠폰 ',
        startAt: '2026-07-01T09:00',
        endAt: '2026-07-31T23:00',
        totalQuantity: '1,000',
        discountAmount: '5,000',
        minOrderAmount: '10,000',
        validDays: '30'
      }),
      {
        type: 'ADMIN_INDIVIDUAL',
        name: '여름 쿠폰',
        startAt: '2026-07-01T09:00',
        endAt: '2026-07-31T23:00',
        totalQuantity: 1000,
        discountAmount: 5000,
        minOrderAmount: 10000,
        validDays: 30
      }
    );
  });

  test('validates required coupon creation fields before API submission', () => {
    const errors = validateAdminCouponForm({
      type: 'FIRST_COME',
      name: '',
      startAt: '2026-08-02T09:00',
      endAt: '2026-08-01T09:00',
      totalQuantity: '0',
      discountAmount: '0',
      minOrderAmount: '-1',
      validDays: '0'
    });

    assert.deepEqual(Object.keys(errors).sort(), [
      'discountAmount',
      'endAt',
      'minOrderAmount',
      'name',
      'totalQuantity',
      'validDays'
    ]);
  });

  test('builds invalid coupon payloads visibly enough for validation to block create', () => {
    const form = {
      type: 'FIRST_COME',
      name: '  ',
      startAt: 'bad-date',
      endAt: '',
      totalQuantity: '0',
      discountAmount: 'abc',
      minOrderAmount: '-100',
      validDays: ''
    };

    assert.deepEqual(buildAdminCouponCreatePayload(form), {
      type: 'FIRST_COME',
      name: '',
      startAt: 'bad-date',
      endAt: '',
      totalQuantity: 0,
      discountAmount: Number.NaN,
      minOrderAmount: -100,
      validDays: 0
    });
    assert.deepEqual(Object.keys(validateAdminCouponForm(form)).sort(), [
      'discountAmount',
      'endAt',
      'minOrderAmount',
      'name',
      'startAt',
      'totalQuantity',
      'validDays'
    ]);
  });

  test('parses issue target ids or nicknames and exposes invalid tokens', () => {
    assert.deepEqual(parseUserIdTokens('1, 2\nabc 3 2 -5 4.5'), {
      validIds: [1, 2, 3],
      validTargets: ['1', '2', 'abc', '3'],
      invalidTokens: ['-5', '4.5']
    });
  });

  test('keeps duplicate invalid issue tokens visible with stable unique chip keys', () => {
    const parsed = parseUserIdTokens('abc abc 7 -5');

    assert.deepEqual(parsed, {
      validIds: [7],
      validTargets: ['abc', '7'],
      invalidTokens: ['-5']
    });
    assert.deepEqual(parsed.invalidTokens.map(getCouponTargetChipKey), ['-5-0']);
  });

  test('requires confirmed detail before admin coupon issue can run', () => {
    assert.equal(canAdminIssueCoupon(null), false);
    assert.equal(canAdminIssueCoupon(undefined), false);
    assert.equal(canAdminIssueCoupon({ canIssue: true, ended: false, soldOut: false }), true);
    assert.equal(canAdminIssueCoupon({ canIssue: false, ended: false, soldOut: false }), false);
    assert.equal(canAdminIssueCoupon({ canIssue: true, ended: true, soldOut: false }), false);
    assert.equal(canAdminIssueCoupon({ canIssue: true, ended: false, soldOut: true }), false);
  });

  test('converts backend issue rate ratio to percent label', () => {
    assert.equal(formatIssueRate(0.375), '38%');
    assert.equal(formatIssueRate(null), '-');
  });
});

describe('admin coupon page source', () => {
  test('renders Korean coupon labels, formatted inputs, target chips, and DTO markers', () => {
    const source = readSource('./AdminPlaceholderPages.jsx');

    assert.match(source, /쿠폰 관리/);
    assert.match(source, /formatAdminCouponType/);
    assert.match(source, /formatAdminCouponStatus/);
    assert.match(source, /formatAdminCouponMoneyInput/);
    assert.match(source, /validateAdminCouponForm/);
    assert.match(source, /coupon-target-chip/);
    assert.match(source, /coupon-target-chip-invalid/);
    assert.match(source, /remainingQuantity/);
    assert.match(source, /formatIssueRate/);
    assert.match(source, /canIssue/);
    assert.match(source, /userEmail/);
  });

  test('requires confirmed detail before enabling admin coupon issue action', () => {
    const source = readSource('./AdminPlaceholderPages.jsx');

    assert.match(
      source,
      /const selectedCanIssue = canAdminIssueCoupon\(detail\);/
    );
    assert.doesNotMatch(source, /const selectedCanIssue = !detail \|\|/);
    assert.match(source, /getCouponTargetChipKey\(token, index\)/);
    assert.doesNotMatch(source, /key=\{token\}/);
  });
});
