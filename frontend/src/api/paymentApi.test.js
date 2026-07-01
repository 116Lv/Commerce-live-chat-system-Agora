import assert from 'node:assert/strict';
import { test } from 'node:test';
import { requestPaymentApproval } from './paymentApi.js';

const basePayment = {
  paymentId: 22,
  orderId: 'order-22',
  amount: 12000,
  buyerEmail: 'buyer@test.com',
  buyerName: '구매자',
  buyerTel: '01033334444'
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

test('requestPaymentApproval sends buyer contact fields to IMP payment window', async () => {
  let payload;
  const result = await requestPaymentApproval(
    {
      ...basePayment,
      buyerEmail: 'buyer@test.com',
      buyerName: '구매자',
      buyerTel: '01033334444'
    },
    {
      window: {
        IMP: {
          request_pay: (request, callback) => {
            payload = request;
            callback({ success: true, imp_uid: 'imp-22' });
          }
        }
      }
    }
  );

  assert.equal(payload.buyer_email, 'buyer@test.com');
  assert.equal(payload.buyer_name, '구매자');
  assert.equal(payload.buyer_tel, '01033334444');
  assert.deepEqual(result, {
    paymentId: 22,
    paymentKey: 'imp-22',
    local: false
  });
});

test('requestPaymentApproval rejects missing buyer phone before opening payment window', async () => {
  let called = false;

  await assert.rejects(
    () =>
      requestPaymentApproval(
        {
          ...basePayment,
          buyerEmail: 'buyer@test.com',
          buyerName: '구매자',
          buyerTel: ''
        },
        {
          window: {
            IMP: {
              request_pay: () => {
                called = true;
              }
            }
          }
        }
      ),
    /구매자 휴대폰 번호를 확인할 수 없습니다\./
  );

  assert.equal(called, false);
});

test('requestPaymentApproval builds PortOne customer from buyer contact fields', async () => {
  let payload;
  const result = await requestPaymentApproval(
    {
      ...basePayment,
      buyerEmail: 'buyer@test.com',
      buyerName: '구매자',
      buyerTel: '01033334444',
      storeId: 'store-1',
      channelKey: 'channel-1'
    },
    {
      window: {
        PortOne: {
          requestPayment: async (request) => {
            payload = request;
            return { paymentKey: 'payment-key-22' };
          }
        }
      }
    }
  );

  assert.deepEqual(payload.customer, {
    email: 'buyer@test.com',
    fullName: '구매자',
    phoneNumber: '01033334444'
  });
  assert.deepEqual(result, {
    paymentId: 22,
    paymentKey: 'payment-key-22',
    local: false
  });
});
