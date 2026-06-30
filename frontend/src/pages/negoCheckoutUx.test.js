import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { describe, test } from 'node:test';

const __dirname = dirname(fileURLToPath(import.meta.url));
const readSource = (relativePath) => readFileSync(resolve(__dirname, relativePath), 'utf8');

describe('negotiation UX source', () => {
  test('NegoPanel removes manual offer id controls and renders per-offer actions', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');

    assert.match(source, /getCurrentOffer/);
    assert.match(source, /getOfferActions/);
    assert.match(source, /runAction\(offer, action\)/);
    assert.match(source, /buildPaymentHref/);
    assert.doesNotMatch(source, /useState\(''\);\s*const \[offerId/);
    assert.doesNotMatch(source, /controlId="offerId"/);
    assert.doesNotMatch(source, /placeholder="offerId"/);
    assert.doesNotMatch(source, /처리할 제안 ID/);
  });

  test('NegoPanel shows accepted-offer checkout CTA and remaining time copy', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');

    assert.match(source, /paymentHref/);
    assert.match(source, /to=\{paymentHref\}/);
    assert.match(source, /getRemainingTimeLabel/);
    assert.match(source, /결제하기/);
  });

  test('NegoPanel keeps product price validation optional until chat metadata exposes price', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');
    const chatRoomSource = readSource('./ChatRoomPage.jsx');

    assert.match(source, /productPrice/);
    assert.match(source, /getOfferPriceValidation/);
    assert.match(source, /const validationMessage = getOfferPriceValidation/);
    assert.doesNotMatch(source, /Number\(offerPrice\)[\s\S]{0,80}\}\);/);
    assert.doesNotMatch(chatRoomSource, /<NegoPanel[\s\S]{0,120}productPrice=/);
  });
});

describe('checkout and payment result UX source', () => {
  test('CheckoutPage uses prepared order information and keeps payment key entry secondary', () => {
    const source = readSource('./CheckoutPage.jsx');

    assert.match(source, /useSearchParams/);
    assert.match(source, /initialPaymentKey/);
    assert.match(source, /checkout-order-summary/);
    assert.match(source, /advanced-payment-confirm/);
    assert.match(source, /기술 승인 정보/);
    assert.doesNotMatch(source, /<Form\.Label>paymentKey<\/Form\.Label>/);
  });

  test('PaymentResultPage auto-confirms URL return values and hides manual key wording', () => {
    const source = readSource('./PaymentResultPage.jsx');

    assert.match(source, /auto-confirm/);
    assert.match(source, /advanced-payment-confirm/);
    assert.match(source, /searchParams\.get\('paymentKey'\)/);
    assert.match(source, /결제사 승인값/);
    assert.doesNotMatch(source, /<Form\.Label>paymentKey<\/Form\.Label>/);
  });
});
