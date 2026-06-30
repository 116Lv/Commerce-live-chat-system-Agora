import assert from 'node:assert/strict';
import { describe, test } from 'node:test';
import {
  buildPaymentHref,
  getOfferActions,
  getOfferPriceValidation
} from './negoPanelUtils.js';

describe('negotiation panel helpers', () => {
  test('keeps seller response actions attached to a pending offer', () => {
    const actions = getOfferActions({ status: 'PENDING' }, { role: 'seller' });

    assert.deepEqual(actions.map((action) => action.key), ['accept', 'reject']);
  });

  test('keeps buyer extension action attached to a pending offer', () => {
    const actions = getOfferActions({ status: 'PENDING' }, { role: 'buyer' });

    assert.deepEqual(actions.map((action) => action.key), ['extension']);
  });

  test('keeps extension approval actions for seller only', () => {
    assert.deepEqual(
      getOfferActions({ status: 'EXTENSION_REQUESTED' }, { role: 'seller' }).map((action) => action.key),
      ['extensionApprove', 'extensionReject']
    );
    assert.deepEqual(
      getOfferActions({ status: 'EXTENSION_REQUESTED' }, { role: 'buyer' }).map((action) => action.key),
      []
    );
  });

  test('falls back to conservative status-aware actions when role is unknown', () => {
    assert.deepEqual(
      getOfferActions({ status: 'PENDING' }, { role: null }).map((action) => action.key),
      ['accept', 'reject', 'extension']
    );
  });

  test('validates positive offer price below product price when product price is available', () => {
    assert.equal(getOfferPriceValidation('0', { productPrice: 50000 }), '제안가는 0원보다 커야 해요.');
    assert.equal(getOfferPriceValidation('50000', { productPrice: 50000 }), '제안가는 상품 가격보다 낮아야 해요.');
    assert.equal(getOfferPriceValidation('45000', { productPrice: 50000 }), '');
  });

  test('validates only positive amount when product price is not available yet', () => {
    assert.equal(getOfferPriceValidation('0'), '제안가는 0원보다 커야 해요.');
    assert.equal(getOfferPriceValidation('50000'), '');
  });

  test('builds checkout href only when accepted offer has a trade id', () => {
    assert.equal(buildPaymentHref({ status: 'ACCEPTED', tradeId: 17 }), '/checkout/17');
    assert.equal(buildPaymentHref({ status: 'PENDING', tradeId: 17 }), '');
    assert.equal(buildPaymentHref({ status: 'ACCEPTED' }), '');
  });
});
