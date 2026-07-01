import assert from 'node:assert/strict';
import { test } from 'node:test';
import { requestPaymentApproval } from './paymentApi.js';

const basePayment = {
  paymentId: 22,
  orderId: 'order-22',
  amount: 12000
};

test('requestPaymentApproval rejects missing SDK when payment is not local or test mode', async () => {
  await assert.rejects(
    () => requestPaymentApproval(basePayment, { window: {} }),
    /사용 가능한 PG 결제 SDK를 찾을 수 없습니다\./
  );
});

test('requestPaymentApproval allows local fallback only for explicit local or test signals', async () => {
  assert.deepEqual(await requestPaymentApproval({ ...basePayment, paymentMode: 'LOCAL' }, { window: {} }), {
    paymentId: 22,
    paymentKey: 'local-order-22',
    local: true
  });

  assert.deepEqual(await requestPaymentApproval({ ...basePayment, pgMode: 'TEST' }, { window: {} }), {
    paymentId: 22,
    paymentKey: 'local-order-22',
    local: true
  });

  assert.deepEqual(await requestPaymentApproval({ ...basePayment, localPayment: true }, { window: {} }), {
    paymentId: 22,
    paymentKey: 'local-order-22',
    local: true
  });
});
