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
  });

  test('NegoPanel shows accepted-offer checkout CTA and remaining time copy', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');

    assert.match(source, /paymentHref/);
    assert.match(source, /to=\{paymentHref\}/);
    assert.match(source, /getRemainingTimeLabel/);
  });

  test('NegoPanel hides checkout after payment and exposes accepted-offer cancel action', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');
    const utilsSource = readSource('../features/nego/negoPanelUtils.js');
    const apiSource = readSource('../api/negoApi.js');

    assert.match(source, /cancelOffer/);
    assert.match(source, /isOfferPayable/);
    assert.match(source, /cancelPaymentPending/);
    assert.match(utilsSource, /tradeStatus/);
    assert.match(utilsSource, /paymentStatus/);
    assert.match(utilsSource, /PAYMENT_PENDING/);
    assert.match(apiSource, /\/cancel/);
  });

  test('NegoPanel limits payment CTA to buyers and shows sellers a payment-pending notice', () => {
    const source = readSource('../features/nego/NegoPanel.jsx');
    const utilsSource = readSource('../features/nego/negoPanelUtils.js');

    assert.match(source, /buildPaymentHref\(offer,\s*\{\s*role:\s*offerRole\s*\}\)/);
    assert.match(source, /isOfferPayable\(offer,\s*\{\s*role:\s*offerRole\s*\}\)/);
    assert.match(source, /offerRole === 'seller'/);
    assert.match(source, /payment-pending-notice/);
    assert.match(utilsSource, /role !== 'buyer'/);
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
  test('CheckoutPage requests provider approval through a payment client boundary', () => {
    const source = readSource('./CheckoutPage.jsx');
    const apiSource = readSource('../api/paymentApi.js');
    const indexSource = readFileSync(resolve(__dirname, '../../index.html'), 'utf8');

    assert.match(source, /checkout-order-summary/);
    assert.match(source, /requestPaymentApproval/);
    assert.match(source, /navigate\(`\/payments\/result\?/);
    assert.match(apiSource, /requestPaymentApproval/);
    assert.match(apiSource, /window\.PortOne|win\.PortOne/);
    assert.match(apiSource, /VITE_PORTONE_STORE_ID/);
    assert.match(apiSource, /VITE_PORTONE_CHANNEL_KEY/);
    assert.match(apiSource, /window\.IMP|win\.IMP/);
    assert.match(indexSource, /https:\/\/cdn\.portone\.io\/v2\/browser-sdk\.js/);
    assert.doesNotMatch(source, /advanced-payment-confirm/);
    assert.doesNotMatch(source, /initialPaymentKey/);
    assert.doesNotMatch(source, /setPaymentKey/);
    assert.doesNotMatch(source, /controlId="paymentKey"/);
    assert.doesNotMatch(source, /confirmPayment\(payment\.paymentId,\s*\{\s*paymentKey:\s*`local-/);
  });

  test('PaymentResultPage only handles provider return values without fallback manual form', () => {
    const source = readSource('./PaymentResultPage.jsx');

    assert.match(source, /auto-confirm/);
    assert.match(source, /searchParams\.get\('paymentKey'\)/);
    assert.doesNotMatch(source, /advanced-payment-confirm/);
    assert.doesNotMatch(source, /setPaymentKey/);
    assert.doesNotMatch(source, /controlId="resultPaymentKey"/);
  });

  test('TradeDetailPage does not expose direct refund form before paid status', () => {
    const source = readSource('./TradeDetailPage.jsx');

    assert.match(source, /paymentStatus === 'PAID'/);
    assert.doesNotMatch(source, /refundPayment/);
    assert.doesNotMatch(source, /controlId="refundPaymentId"/);
    assert.doesNotMatch(source, /type="submit"[\s\S]{0,80}refund/);
  });

  test('TradeDetailPage limits reviews to buyers and shows counterpart smile score', () => {
    const source = readSource('./TradeDetailPage.jsx');

    assert.match(source, /getSmileScore/);
    assert.match(source, /counterpartSmileScore/);
    assert.match(source, /const canReviewTrade = isCurrentUserBuyer && tradeStatus === 'COMPLETED'/);
    assert.doesNotMatch(source, /const canReviewTrade = isCurrentUserParticipant && tradeStatus === 'COMPLETED'/);
  });
});
