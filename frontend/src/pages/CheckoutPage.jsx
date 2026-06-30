import { useEffect, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { confirmPayment, preparePayment } from '../api/paymentApi.js';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, statusText } from './pageUtils.jsx';

export default function CheckoutPage() {
  const { tradeId } = useParams();
  const [searchParams] = useSearchParams();
  const initialPaymentKey = searchParams.get('paymentKey') || '';
  const [payment, setPayment] = useState(null);
  const [paymentKey, setPaymentKey] = useState(initialPaymentKey);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [confirming, setConfirming] = useState(false);
  const [autoConfirmAttempted, setAutoConfirmAttempted] = useState(false);

  useEffect(() => {
    let disposed = false;

    const prepare = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await preparePayment(tradeId);
        if (!disposed) {
          setPayment(response);
        }
      } catch (err) {
        if (!disposed) {
          setError(err.message);
        }
      } finally {
        if (!disposed) {
          setLoading(false);
        }
      }
    };

    prepare();

    return () => {
      disposed = true;
    };
  }, [tradeId]);

  const confirmPreparedPayment = async (approvalKey = paymentKey) => {
    if (!payment?.paymentId || !approvalKey.trim()) {
      return;
    }

    setMessage('');
    setError('');
    setConfirming(true);

    try {
      const response = await confirmPayment(payment.paymentId, { paymentKey: approvalKey.trim() });
      setPayment(response);
      setMessage('결제가 확인됐어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirming(false);
    }
  };

  useEffect(() => {
    if (payment && initialPaymentKey && !autoConfirmAttempted) {
      setAutoConfirmAttempted(true);
      confirmPreparedPayment(initialPaymentKey);
    }
  }, [autoConfirmAttempted, initialPaymentKey, payment]);

  const handleConfirm = async (event) => {
    event.preventDefault();
    await confirmPreparedPayment();
  };

  return (
    <section>
      <PageHeader title="결제" eyebrow="Checkout" />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <div className="detail-panel">
        {loading ? <p className="mb-0">결제 준비 중...</p> : null}
        {!loading && payment ? (
          <>
            <div className="checkout-order-summary d-flex justify-content-between align-items-start gap-2">
              <div>
                <h2 className="section-title">주문 결제 준비</h2>
                <p className="text-muted mb-0">주문 {payment.orderId || '-'}</p>
              </div>
              <StatusBadge status={payment.status} />
            </div>
            <MoneyText amount={payment.amount} className="product-detail-price mt-2" />
            <dl className="compact-list mt-3">
              <div>
                <dt>거래</dt>
                <dd>{payment.tradeId}</dd>
              </div>
              <div>
                <dt>상태</dt>
                <dd>{statusText(payment.status)}</dd>
              </div>
            </dl>
            <p className="text-muted mt-3 mb-0">
              결제창을 완료하면 결과 화면으로 돌아와 자동으로 승인됩니다. 돌아온 주소에 승인값이 포함되어 있으면 여기서도 바로 확인해요.
            </p>
            <details className="advanced-payment-confirm mt-3">
              <summary>기술 승인 정보 직접 입력</summary>
              <Form onSubmit={handleConfirm} className="stack-list mt-3">
                <Form.Group controlId="paymentKey">
                  <Form.Label>결제사 승인값</Form.Label>
                  <Form.Control
                    value={paymentKey}
                    onChange={(event) => setPaymentKey(event.target.value)}
                    placeholder="결제 완료 후 받은 승인값"
                    required
                  />
                </Form.Group>
                <div className="form-actions">
                  <Button as={Link} to={`/trades/${tradeId}`} variant="outline-secondary">
                    거래로 이동
                  </Button>
                  <Button type="submit" disabled={confirming || !paymentKey.trim()}>
                    {confirming ? '확인 중' : '승인 확인'}
                  </Button>
                </div>
              </Form>
            </details>
          </>
        ) : null}
      </div>
    </section>
  );
}
